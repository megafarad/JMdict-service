package com.megafarad.jmdictsvc.component

import com.megafarad.jmdictsvc.model.repository.EntryRepositoryComponent

trait Repositories {
  def entryRepository: EntryRepositoryComponent
}
