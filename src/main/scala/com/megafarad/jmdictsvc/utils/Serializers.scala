package com.megafarad.jmdictsvc.utils

import com.github.pjfanning.pekkohttpjson4s.Json4sSupport
import org.json4s.native.Serialization
import org.json4s.{Formats, NoTypeHints, jackson}

trait Serializers extends Json4sSupport {

  implicit val serialization: jackson.Serialization.type = jackson.Serialization

  implicit val formats: Formats = Serialization.formats(NoTypeHints)
}
