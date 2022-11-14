package com.megafarad.jmdictsvc.http

import com.megafarad.jmdictsvc.component.{ActorSystemComponent, ConfigComponent, Repositories}
import com.megafarad.jmdictsvc.http.service.EntryService
import com.megafarad.jmdictsvc.utils.{Logging, Serializers}

trait HttpRoutingService extends HttpBaseService with EntryService  {
  this: Repositories with ActorSystemComponent with Serializers with ConfigComponent with Logging with HttpRoute =>
}
