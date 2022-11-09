package com.megafarad.jmdictsvc.model.repository

import com.megafarad.jmdictsvc.model.db.EntryJsonTableComponent
import com.megafarad.jmdictsvc.model.repository.EntryRepositoryComponent
import org.scalatest.{BeforeAndAfterAll, Suite}
import slick.jdbc.JdbcBackend.Database
import slick.jdbc.PostgresProfile

import scala.concurrent.Await
import scala.concurrent.duration.Duration

trait DatabaseSpec extends Suite with BeforeAndAfterAll {
  lazy val db = Database.forConfig("database.postgre")
  lazy val profile = new PostgresProfile {}
  lazy val entryRepository: EntryRepositoryComponent = ExtendedEntryRepository(db, profile)
  private val schemaName = "jmdict"

  import profile.api._

  override def beforeAll(): Unit = {
    val table: EntryJsonTableComponent = entryRepository.asInstanceOf[ExtendedEntryRepository].getEntryTable
    val run = db.run{
      DBIO.seq(sqlu"CREATE SCHEMA IF NOT EXISTS #$schemaName", table.entryIndexes.schema.dropIfExists,
        table.entryJsons.schema.dropIfExists, table.entryJsons.schema.create, table.entryIndexes.schema.create)
    }

    Await.result(run, Duration.Inf)

  }
}
