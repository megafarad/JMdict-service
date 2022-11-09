package com.megafarad.jmdictsvc.component.impl

import akka.http.scaladsl.Http
import com.megafarad.jmdictsvc.component._
import com.megafarad.jmdictsvc.http.HttpRoute

trait RoutingComponentImpl extends RoutingComponent {
  this: ConfigComponent with ActorSystemComponent with Repositories with HttpRoute =>

  private val host = config.getString("application.host")
  private val port = config.getInt("application.port")

  Http().newServerAt(host, port).bind(route)
}
