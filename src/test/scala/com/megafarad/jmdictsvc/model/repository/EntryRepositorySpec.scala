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
    val fishJson = Source.fromResource("sakana.json").mkString("")
    val fishEntry = read[Entry](fishJson)
    val houseJson = Source.fromResource("ie.json").mkString("")
    val houseEntry = read[Entry](houseJson)

    "should add entry for fish successfully" in {
      entryRepository.upsert(fishEntry).map {
        _ => succeed
      }
    }
    "should find entry for fish by kanji" in {
      entryRepository.search("魚", 0, 10).map {
        foundEntries =>
          foundEntries.size should be (1)
          foundEntries.headOption should contain (fishEntry)
      }
    }

    "should find entry for fish by reading" in {
      entryRepository.search("さかな", 0, 10).map {
        foundEntries =>
          foundEntries.size should be (1)
          foundEntries.headOption should contain (fishEntry)
      }
    }

    "should find entry for fish by meaning" in {
      entryRepository.search("fish", 0, 10).map {
        foundEntries =>
          foundEntries.size should be (1)
          foundEntries.headOption should contain (fishEntry)
      }
    }

    "should add entry for house successfully" in {
      entryRepository.upsert(houseEntry).map {
        _ => succeed
      }
    }

    "should find entry for house by meaning" in {
      entryRepository.search("house",0, 10).map {
        foundEntries =>
          foundEntries.size should be(1)
          foundEntries.headOption should contain (houseEntry)
      }
    }
  }

}
