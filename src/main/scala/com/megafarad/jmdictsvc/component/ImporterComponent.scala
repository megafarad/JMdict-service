package com.megafarad.jmdictsvc.component

import com.megafarad.jmdictsvc.actor.Importer
import akka.actor.typed.ActorRef
import com.typesafe.akka.extension.quartz.QuartzSchedulerTypedExtension

import java.util.Date

trait ImporterComponent {
  val importerActorRef: ActorRef[Importer.Import]
  val scheduler: QuartzSchedulerTypedExtension
  val firstFire: Date
}
