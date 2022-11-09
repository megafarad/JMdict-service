package com.megafarad.jmdictsvc.actor

import akka.Done
import com.megafarad.jmdictsvc.component.Repositories
import com.megafarad.jmdictsvc.model.entity.Entry
import akka.actor.typed._
import akka.actor.typed.scaladsl._
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, Uri}
import akka.stream.scaladsl.{FileIO, Sink, Source}
import akka.stream.{IOResult, Materializer}
import com.megafarad.jmdictsvc.model.repository.EntryRepositoryComponent
import com.megafarad.jmdictsvc.utils.Serializers
import org.json.XMLParserConfiguration
import org.json4s.jackson.Serialization.read

import java.io.{File, FileInputStream}
import java.util
import java.util.Properties
import java.util.zip.GZIPInputStream
import javax.xml.stream.XMLInputFactory
import javax.xml.stream.events.XMLEvent
import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

object Importer extends Serializers {

  sealed trait Import

  final case class DownloadFile(source: String) extends Import

  final case class ParseFile(tempFile: File) extends Import

  final case class ParseEvent(xmlEvent: XMLEvent) extends Import

  final case class ImportEntry(entry: Entry) extends Import

  final case class SuccessfulImport(entry: Entry) extends Import

  def apply(entryRepository: EntryRepositoryComponent): Behavior[Import] =
    importer(entryRepository, new mutable.StringBuilder())

  private def importer(entryRepository: EntryRepositoryComponent, elementBuilder: mutable.StringBuilder): Behavior[Import] =
    Behaviors.setup[Import] { context =>
    Behaviors.receiveMessage {
      case DownloadFile(source) =>
        implicit val system: ActorSystem[Nothing] = context.system
        implicit val ec: ExecutionContext = system.executionContext
        context.log.info("Begin import")
        val file = File.createTempFile("jmdict", ".gz")
        file.deleteOnExit()
        context.log.info("downloading to... {}", file.toString)
        context.pipeToSelf(downloadViaFlow(Uri(source), file)) {
          case Failure(_) => DownloadFile(source)
          case Success(_) => ParseFile(tempFile = file)
        }
        Behaviors.same
      case ParseFile(tempFile) =>
        context.log.info("parsing file... {}", tempFile)
        val fis: FileInputStream = new FileInputStream(tempFile.toString)
        val gis: GZIPInputStream = new GZIPInputStream(fis)
        val props: Properties = System.getProperties
        props.setProperty("jdk.xml.entityExpansionLimit", "0")
        val xmlInputFactory = XMLInputFactory.newInstance()
        val reader = xmlInputFactory.createXMLEventReader(gis)
        while (reader.hasNext) {
          context.self ! ParseEvent(xmlEvent = reader.nextEvent())
        }
        tempFile.delete()
        Behaviors.same
      case ParseEvent(xmlEvent) =>
        if (xmlEvent.isStartElement) {
          val startElement = xmlEvent.asStartElement()
          startElement.getName.getLocalPart match {
            case "JMdict" => context.log.info("Start Import")
            case _ => elementBuilder.append(startElement.toString)
          }
        } else if (xmlEvent.isCharacters) {
          val characters = xmlEvent.asCharacters()
          elementBuilder.append(characters.toString
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("\'", "&apos;"))
        } else if (xmlEvent.isEndElement) {
          val endElement = xmlEvent.asEndElement()
          endElement.getName.getLocalPart match {
            case "JMdict" => context.log.info("End Parsing Input")
            case "entry" =>
              elementBuilder.append(endElement.toString)
              val forceList = new util.HashSet[String]()
              forceList.add("k_ele")
              forceList.add("r_ele")
              forceList.add("sense")
              forceList.add("ke_inf")
              forceList.add("ke_pri")
              forceList.add("re_restr")
              forceList.add("re_inf")
              forceList.add("re_pri")
              forceList.add("links")
              forceList.add("bibl")
              forceList.add("etym")
              forceList.add("audit")
              forceList.add("stagk")
              forceList.add("stagr")
              forceList.add("pos")
              forceList.add("xref")
              forceList.add("ant")
              forceList.add("field")
              forceList.add("misc")
              forceList.add("s_inf")
              forceList.add("lsource")
              forceList.add("dial")
              forceList.add("gloss")
              forceList.add("example")
              //TODO: don't like this. Would rather parse out XML and rework that way...
              val cleanXML = elementBuilder.toString().replace("xml:lang=", "lang=")
                .replace("<re_nokanji></re_nokanji>", "<re_nokanji>true</re_nokanji>")
              val json = org.json.XML.toJSONObject(cleanXML, new XMLParserConfiguration().withForceList(forceList).withKeepStrings(true))
              val entryJson = json.getJSONObject("entry")
              elementBuilder.clear()
              val entry = read[Entry](entryJson.toString)
              context.self ! ImportEntry(entry)
            case _ => elementBuilder.append(endElement)
          }
        }
        importer(entryRepository, elementBuilder)
      case ImportEntry(entry) =>
        context.pipeToSelf(entryRepository.upsert(entry)) {
          case Failure(_) => ImportEntry(entry)
          case Success(_) => SuccessfulImport(entry)
        }
        Behaviors.same
      case SuccessfulImport(entry) =>
        context.log.debug("Imported entry: {}", entry)
        Behaviors.same
    }
  }

  private def writeFile(file: File)(httpResponse: HttpResponse)(implicit materializer: Materializer): Future[IOResult] = {
    httpResponse.entity.dataBytes.runWith(FileIO.toPath(file.toPath))
  }

  private def responseOrFail[T](in: (Try[HttpResponse], T)): (HttpResponse, T) = in match {
    case (responseTry, context) => (responseTry.get, context)
  }

  private def downloadViaFlow(uri: Uri, downloadFile: File)(implicit system: ActorSystem[Nothing]): Future[Done] = {
    val request = HttpRequest(uri = uri)
    val source = Source.single((request, ()))
    val requestResponseFlow = Http().superPool[Unit]()
    val parallelism = 10
    source.via(requestResponseFlow)
      .map(responseOrFail)
      .map(_._1)
      .mapAsyncUnordered(parallelism)(writeFile(downloadFile))
      .runWith(Sink.ignore)
  }
}
