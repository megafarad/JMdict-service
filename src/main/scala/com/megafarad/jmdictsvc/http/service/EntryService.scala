package com.megafarad.jmdictsvc.http.service

import com.megafarad.jmdictsvc.component.{ActorSystemComponent, Repositories}
import akka.http.scaladsl.server.Directives._
import com.megafarad.jmdictsvc.http.HttpBaseService
import com.megafarad.jmdictsvc.model.entity.Entry
import com.megafarad.jmdictsvc.utils.Serializers

trait EntryService {
  this: Repositories with HttpBaseService with ActorSystemComponent with Serializers =>

  registerRoute(pathPrefix("api" / "search") {
    get {
      path(Segment) {  query: String =>
        handleResponse[Seq[Entry]](entryRepository.search(query.trim))
      }
    }
  })
}
