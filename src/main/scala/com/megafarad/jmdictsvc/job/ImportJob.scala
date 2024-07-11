package com.megafarad.jmdictsvc.job

import org.apache.pekko.Done
import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.http.scaladsl.Http
import org.apache.pekko.http.scaladsl.model.{HttpRequest, HttpResponse, Uri}
import org.apache.pekko.stream.scaladsl.{FileIO, Sink, Source}
import org.apache.pekko.stream.{IOResult, Materializer}
import com.megafarad.jmdictsvc.model.entity.Entry
import com.megafarad.jmdictsvc.model.repository.EntryRepositoryComponent
import org.json.XMLParserConfiguration
import org.json4s.{Formats, NoTypeHints}
import org.json4s.jackson.Serialization.read
import org.json4s.native.Serialization
import org.quartz.{Job, JobDataMap, JobExecutionContext}
import org.slf4j.Logger

import java.io.{File, FileInputStream}
import java.util
import java.util.Properties
import java.util.zip.GZIPInputStream
import javax.xml.stream.XMLInputFactory
import scala.collection.mutable
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.Try

class ImportJob extends Job {
  protected def as[T](key: String)(implicit dataMap: JobDataMap): T = Option(dataMap.get(key)) match {
    case Some(item) => item.asInstanceOf[T]
    case None => throw new NoSuchElementException("No entry in JobDataMap for required entry '%s'".format(key))
  }
  override def execute(context: JobExecutionContext): Unit = {
    implicit val jobDataMap: JobDataMap = context.getJobDetail.getJobDataMap
    implicit val system: ActorSystem[Nothing] = as[ActorSystem[Nothing]]("actorSystem")
    implicit val ec: ExecutionContext = system.executionContext
    implicit val formats: Formats = Serialization.formats(NoTypeHints)
    val entryRepository: EntryRepositoryComponent = as[EntryRepositoryComponent]("entryRepository")
    val source = as[String]("source")
    val log = as[Logger]("logger")

    val tempFile = File.createTempFile("jmdict", ".gz")
    log.info("Created temp file: {}", tempFile)
    downloadViaFlow(Uri(source), tempFile).map {
      _ =>
        log.info("Download complete.")
        log.info("parsing file: {}", tempFile)
        val fis: FileInputStream = new FileInputStream(tempFile.toString)
        val gis: GZIPInputStream = new GZIPInputStream(fis)
        val props: Properties = System.getProperties
        props.setProperty("jdk.xml.entityExpansionLimit", "0")
        val xmlInputFactory = XMLInputFactory.newInstance()
        val reader = xmlInputFactory.createXMLEventReader(gis)
        val elementBuilder = new mutable.StringBuilder()
        while (reader.hasNext) {
          val xmlEvent = reader.nextEvent()
          if (xmlEvent.isStartElement) {
            val startElement = xmlEvent.asStartElement()
            startElement.getName.getLocalPart match {
              case "JMdict" => log.info("Start Import")
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
              case "JMdict" => log.info("End Parsing Input")
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
                try {
                  Await.result(entryRepository.upsert(entry), FiniteDuration(60,"s"))
                  log.debug("Upserted entry: {}", entry)
                } catch {
                  case e: Exception => log.error("Unable to upsert entry. ", e)
                }
              case _ => elementBuilder.append(endElement)
            }
          }
        }
        log.info("Complete import")
        tempFile.delete()
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
