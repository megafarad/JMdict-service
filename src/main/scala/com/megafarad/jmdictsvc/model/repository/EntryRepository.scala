package com.megafarad.jmdictsvc.model.repository

import com.megafarad.jmdictsvc.model.db.EntryJsonTableComponent
import com.megafarad.jmdictsvc.model.entity._
import org.json4s._
import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization.{read, write}
import com.moji4j.MojiDetector
import slick.jdbc.JdbcBackend.Database
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}


trait EntryRepositoryComponent {
  def upsert(entry: Entry): Future[Unit]
  def search(query: String): Future[Seq[Entry]]
}

object EntryRepository {
  def apply(db: Database, profile: JdbcProfile): EntryRepository = new EntryRepository(db, profile)
}

class EntryRepository(db: Database, profile: JdbcProfile) extends EntryRepositoryComponent {

  protected val entryJsonTable: EntryJsonTableComponent = EntryJsonTableComponent(profile)

  import profile.api._
  import entryJsonTable.entryIndexes
  import entryJsonTable.entryJsons

  implicit val formats: Formats = Serialization.formats(NoTypeHints)
  implicit val ec: ExecutionContext = db.ioExecutionContext

  entryJsonTable.createTablesIfNotExist(db)

  override def upsert(entry: Entry): Future[Unit] = {
    val (json, entryIndexesToUpdate) = getDBRows(entry)

    val dbActions = DBIO.seq(
      entryJsons.insertOrUpdate(json),
      DBIO.sequence(entryIndexesToUpdate.map(entryIndexes.insertOrUpdate))
    ).transactionally
    db.run(dbActions)
  }

  override def search(query: String): Future[Seq[Entry]] = {

    val detector = new MojiDetector
    val containsKanji = detector.hasKanji(query)
    val onlyKana = query.forall(detector.isKana)

    if (containsKanji) {
      val exactMatch = for {
        matchingIndexes <- entryIndexes.filter(_.kanji === query).sortBy(_.priPoint)
        jsons <- entryJsons if matchingIndexes.id === jsons.entrySeq
      } yield (jsons, matchingIndexes)

      val likeMatch = for {
        matchingIndexes <- entryIndexes.filter(_.kanji like s"%$query%").sortBy(_.priPoint)
        jsons <- entryJsons if matchingIndexes.id === jsons.entrySeq
      } yield (jsons, matchingIndexes)

      val matches = (exactMatch union likeMatch).distinctOn(_._1.json)

      db.run(matches.result).map {
        results => results.map {
          case (EntryJson(_, json), EntryIndex(_, _, _, _, _)) => read[Entry](json)
        }
      }
    } else if (onlyKana) {
      val exactMatch = for {
        matchingIndexes <- entryIndexes.filter(_.reading === query).sortBy(_.priPoint)
        jsons <- entryJsons if matchingIndexes.id === jsons.entrySeq
      } yield (jsons, matchingIndexes)

      val likeMatch = for {
        matchingIndexes <- entryIndexes.filter(_.reading like s"%$query%").sortBy(_.priPoint)
        jsons <- entryJsons if matchingIndexes.id === jsons.entrySeq
      } yield (jsons, matchingIndexes)

      val matches = (exactMatch union likeMatch).distinctOn(_._1.json)

      db.run(matches.result).map {
        results => results.map {
          case (EntryJson(_, json), EntryIndex(_, _, _, _, _)) => read[Entry](json)
        }
      }
    } else {
      val bestMatch = for {
        matchingIndexes <- entryIndexes.filter(_.meaning like s"%$query;%").sortBy(_.priPoint)
        jsons <- entryJsons if matchingIndexes.id === jsons.entrySeq
      } yield (jsons, matchingIndexes)

      val likeMatch = for {
        matchingIndexes <- entryIndexes.filter(_.meaning like s"%$query%").sortBy(_.priPoint)
        jsons <- entryJsons if matchingIndexes.id === jsons.entrySeq
      } yield (jsons, matchingIndexes)

      val matches = (bestMatch union likeMatch).distinctOn(_._1.json)

      db.run(matches.result).map {
        results => results.map {
          case (EntryJson(_, json), EntryIndex(_, _, _, _, _)) => read[Entry](json)
        }
      }
    }

  }

  private def getDBRows(entry: Entry): (EntryJson, Seq[EntryIndex]) = {
    val entryJson: EntryJson = EntryJson(entry.ent_seq, write(entry))
    val entrySeq: String = entry.ent_seq

    val rElePriPoints: Map[String, Int] = entry.r_ele.map {
      rEle => rEle.reb -> calculatePriority(rEle.re_pri.toSeq.flatten)
    }.toMap

    val entryIndexes = entry.k_ele match {
      case None => entry.r_ele.map {
        r_ele =>
          EntryIndex(
            id = entrySeq,
            kanji = "*",
            reading = r_ele.reb,
            priPoint = rElePriPoints(r_ele.reb),
            meaning = "")
      }
      case Some(kEleSeq) => kEleSeq.flatMap {
        kEle =>
          val keb = kEle.keb
          val kElePriPoint = calculatePriority(kEle.ke_pri.toSeq.flatten)
          entry.r_ele.flatMap {
            rEle =>
              val reb = rEle.reb
              val priPoint = Math.max(kElePriPoint, rElePriPoints(reb))
              if (rEle.re_nokanji.nonEmpty) {
                Nil
              } else {
                rEle.re_restr match {
                  case Some(reRestr) => if (reRestr.contains(keb)) {
                    Seq(EntryIndex(
                      id = entrySeq,
                      kanji = keb,
                      reading = reb,
                      priPoint = priPoint,
                      meaning = ""))
                  } else Nil
                  case None => Seq(EntryIndex(
                    id = entrySeq,
                    kanji = keb,
                    reading = reb,
                    priPoint = priPoint,
                    meaning = ""))
                }
              }
          }
      }
    }

    val withNoKanji = entryIndexes ++ entry.r_ele.collect {
      case rEle if rEle.re_nokanji.nonEmpty =>
        val reb = rEle.reb

        val priPoint = rElePriPoints(reb)
        EntryIndex(id = entrySeq, kanji = "*", reading = reb, priPoint = priPoint, meaning = "")
    }

    val withMeanings = entry.sense.foldLeft[Seq[EntryIndex]](withNoKanji) {
      (entries, sense: Sense) =>
        val glosses = sense.gloss.toSeq.flatten.map(_.content).mkString("; ") + "; "
        entries.map {
          entryIndex =>
            if (sense.stagk.isEmpty && sense.stagr.isEmpty) {
              entryIndex.copy(meaning = entryIndex.meaning + glosses)
            } else if (sense.stagk.toSeq.flatten.exists(entryIndex.kanji.contains(_))) {
              entryIndex.copy(meaning = entryIndex.meaning + glosses)
            } else if (sense.stagr.toSeq.flatten.contains(entryIndex.reading)) {
              entryIndex.copy(meaning = entryIndex.meaning + glosses)
            } else entryIndex
        }
    }
    (entryJson, withMeanings)
  }


  private def calculatePriority(priArray: Seq[String]): Int = {
    val withNews: Int = if (priArray.contains("news1")) 0
    else if (priArray.contains("news2")) 12001
    else 24001
    val withIchi: Int = if (priArray.contains("ichi1")) withNews
    else if (priArray.contains("ichi2")) withNews + 9401
    else withNews + 9501
    val withSpec: Int = if (priArray.contains("spec1")) withIchi
    else if (priArray.contains("spec2")) withIchi + 1601
    else withIchi + 3201
    val withGai = if (priArray.contains("gai1")) withSpec
    else if (priArray.contains("gai2")) withSpec + 4200
    else withSpec + 4410
    priArray.find(_.startsWith("nf")) match {
      case Some(value) => (value.substring(2, 4).toInt + 1) * 500 + 1
      case None => withGai + 23541
    }

  }
}
