package com.megafarad.jmdictsvc.http.service

import com.megafarad.jmdictsvc.component.{ActorSystemComponent, ConfigComponent, Repositories}
import org.apache.pekko.http.scaladsl.server.Directives._
import com.megafarad.jmdictsvc.http.HttpBaseService
import com.megafarad.jmdictsvc.model.entity.Entry
import com.megafarad.jmdictsvc.utils.{Logging, Serializers}

trait EntryService {
  this: Repositories with HttpBaseService with ActorSystemComponent with ConfigComponent with Logging with Serializers =>

  val auth0enabled: Boolean = config.getBoolean("auth0.enabled")
  lazy val auth0domain: String = config.getString("auth0.domain")
  lazy val auth0Audience: String = config.getString("auth0.audience")

  registerRoute(pathPrefix("api" / "search") {
    get {
      path(Segment) {  query: String =>
        if (auth0enabled) {
          authenticateOAuth2("KobuKobu", Auth0Authenticator(auth0domain, auth0Audience)) {
            _ => handleResponse[Seq[Entry]](entryRepository.search(query.trim))
          }
        } else handleResponse[Seq[Entry]](entryRepository.search(query.trim))
      }
    }
  })
}
