package com.megafarad.jmdictsvc.component

import com.typesafe.config.Config

trait ConfigComponent {
  def config: Config
}
