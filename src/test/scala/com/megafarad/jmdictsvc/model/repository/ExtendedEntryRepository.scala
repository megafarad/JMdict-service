package com.megafarad.jmdictsvc.model.repository

import com.megafarad.jmdictsvc.model.db.EntryJsonTableComponent
import com.megafarad.jmdictsvc.model.repository.EntryRepository
import slick.jdbc.JdbcBackend.Database
import slick.jdbc.JdbcProfile

object ExtendedEntryRepository {
  def apply(db: Database, profile: JdbcProfile) = new ExtendedEntryRepository(db, profile)
}

class ExtendedEntryRepository(db: Database, profile: JdbcProfile) extends EntryRepository(db, profile) {
  def getEntryTable: EntryJsonTableComponent = entryJsonTable
}
