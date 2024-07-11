package com.megafarad.jmdictsvc.model.db

import com.megafarad.jmdictsvc.model.entity._
import slick.jdbc.JdbcProfile
import slick.lifted.{ForeignKeyQuery, PrimaryKey, ProvenShape}

import scala.concurrent.Future

object EntryJsonTableComponent {

  def apply(profile: JdbcProfile): EntryJsonTableComponent = new EntryJsonTableComponent(profile)

}


class EntryJsonTableComponent(val profile: JdbcProfile) {

  import profile.api._

  class EntryJsonTable(tag: Tag) extends Table[EntryJson](tag, Some("jmdict"),"entry_json") {

    def entrySeq: Rep[String] = column("ent_seq", O.PrimaryKey)

    def json: Rep[String] = column("json")

    def * : ProvenShape[EntryJson] =
      (entrySeq, json) <> (EntryJson.tupled, EntryJson.unapply)
  }

  val entryJsons: TableQuery[EntryJsonTable] = TableQuery[EntryJsonTable]

  class EntryIndexTable(tag: Tag) extends Table[EntryIndex](tag, Some("jmdict"), "entry_index") {

    def id: Rep[String] = column[String]("id")

    def kanji: Rep[String] = column[String]("kanji")

    def reading: Rep[String] = column[String]("reading")

    def priPoint: Rep[Int] = column[Int]("pri_point")

    def meaning: Rep[String] = column[String]("meaning")

    def * : ProvenShape[EntryIndex] =
      (id, kanji, reading, priPoint, meaning) <> (EntryIndex.tupled, EntryIndex.unapply)

    def pk: PrimaryKey = primaryKey("pk_a", (id, kanji, reading))

    def fk: ForeignKeyQuery[EntryJsonTable, EntryJson] = foreignKey("fk_a", id, entryJsons)(_.entrySeq)

  }

  val entryIndexes: TableQuery[EntryIndexTable] = TableQuery[EntryIndexTable]

}
