package com.megafarad.jmdictsvc

import com.megafarad.jmdictsvc.component.impl._
import com.megafarad.jmdictsvc.http.{HttpRoute, HttpRoutingService}
import com.megafarad.jmdictsvc.utils.{Logging, Serializers}

object Boot
  extends App
    with Logging
    with ConfigComponentImpl
    with ActorSystemComponentImpl
    with DatabaseComponentImpl
    with RepositoriesImpl
    with ImporterComponentImpl
    with Serializers
    with HttpRoute
    with HttpRoutingService
    with RoutingComponentImpl
