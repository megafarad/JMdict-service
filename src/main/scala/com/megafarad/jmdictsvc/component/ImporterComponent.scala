package com.megafarad.jmdictsvc.component

import org.quartz.Scheduler

import java.util.Date

trait ImporterComponent {
  val scheduler: Scheduler
  val firstFire: Date
}
