package com.megafarad.jmdictsvc.component.impl

import com.megafarad.jmdictsvc.component.{ActorSystemComponent, DatabaseComponent, Repositories}
import com.megafarad.jmdictsvc.model.repository.{EntryRepository, EntryRepositoryComponent}
import com.megafarad.jmdictsvc.utils.Logging

import scala.util.{Failure, Success}

trait RepositoriesImpl extends Repositories {
  this: DatabaseComponent with Logging with ActorSystemComponent =>

  override def entryRepository: EntryRepositoryComponent = EntryRepository(db, profile)

  entryRepository.initializeDBTables.onComplete {
    case Failure(exception) =>
      log.error("Unable to initialize tables.", exception)
    case Success(_) =>
      log.info("Successfully initialized tables.")
  }

}
