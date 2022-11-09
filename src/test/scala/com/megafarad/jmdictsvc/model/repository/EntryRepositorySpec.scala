package com.megafarad.jmdictsvc.model.repository

import com.megafarad.jmdictsvc.model.entity.Entry
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers
import org.json4s._
import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization.{read, write}

import scala.io.Source

class EntryRepositorySpec extends AsyncFreeSpec with DatabaseSpec with Matchers {
  implicit val formats: Formats = Serialization.formats(NoTypeHints)

  "EntryRepositorySpec" - {
    val entryJson = Source.fromResource("sakana.json").mkString("")
    val entry = read[Entry](entryJson)
    "should add entry successfully" in {
      entryRepository.upsert(entry).map {
        _ => succeed
      }
    }
    "should find entry by kanji" in {
      entryRepository.search("魚").map {
        foundEntries =>
          foundEntries.size should be (1)
          foundEntries.headOption should contain (entry)
      }
    }

    "should find entry by reading" in {
      entryRepository.search("さかな").map {
        foundEntries =>
          foundEntries.size should be (1)
          foundEntries.headOption should contain (entry)
      }
    }

    "should find entry by meaning" in {
      entryRepository.search("fish").map {
        foundEntries =>
          foundEntries.size should be (1)
          foundEntries.headOption should contain (entry)
      }
    }
  }

}
