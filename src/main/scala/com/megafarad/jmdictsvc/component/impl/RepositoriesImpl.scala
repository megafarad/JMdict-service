package com.megafarad.jmdictsvc.component.impl

import com.megafarad.jmdictsvc.component.{ActorSystemComponent, DatabaseComponent, Repositories}
import com.megafarad.jmdictsvc.model.repository.{EntryRepository, EntryRepositoryComponent}
import com.megafarad.jmdictsvc.utils.Logging

trait RepositoriesImpl extends Repositories {
  this: DatabaseComponent with Logging with ActorSystemComponent =>

  override def entryRepository: EntryRepositoryComponent = EntryRepository(db, profile)


}
