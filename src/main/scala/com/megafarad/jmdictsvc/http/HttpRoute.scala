package com.megafarad.jmdictsvc.http

import akka.http.scaladsl.server.Route

trait HttpRoute {
  def route: Route
}
