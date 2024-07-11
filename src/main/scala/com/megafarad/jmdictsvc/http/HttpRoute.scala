package com.megafarad.jmdictsvc.http

import org.apache.pekko.http.scaladsl.server.Route

trait HttpRoute {
  def route: Route
}
