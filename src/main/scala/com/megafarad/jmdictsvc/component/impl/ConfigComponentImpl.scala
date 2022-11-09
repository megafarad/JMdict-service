package com.megafarad.jmdictsvc.component.impl

import com.megafarad.jmdictsvc.component.ConfigComponent
import com.typesafe.config.{Config, ConfigFactory}

trait ConfigComponentImpl extends ConfigComponent {
  override def config: Config = ConfigFactory.load()
}
