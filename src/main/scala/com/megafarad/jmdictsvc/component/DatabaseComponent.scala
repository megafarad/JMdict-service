package com.megafarad.jmdictsvc.component

import slick.jdbc.JdbcBackend.Database
import slick.jdbc.JdbcProfile

trait DatabaseComponent {
  def db: Database
  def profile: JdbcProfile
}
