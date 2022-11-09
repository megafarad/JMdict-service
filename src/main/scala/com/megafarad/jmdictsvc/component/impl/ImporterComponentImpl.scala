package com.megafarad.jmdictsvc.component.impl

import akka.actor.typed._
import akka.actor.typed.scaladsl.adapter._
import com.megafarad.jmdictsvc.actor.Importer
import com.megafarad.jmdictsvc.component._
import com.typesafe.akka.extension.quartz.QuartzSchedulerTypedExtension

import java.util.{Date, TimeZone}

trait ImporterComponentImpl extends ImporterComponent {
  this: ActorSystemComponent with DatabaseComponent with ConfigComponent with Repositories  =>
  private val schedule = config.getString("importer.schedule")
  private val source = config.getString("importer.source")
  private val typedSystem = actorSystem.toTyped

  override val importerActorRef: ActorRef[Importer.Import] = actorSystem.spawn(Importer(entryRepository), "importer")
  override val scheduler: QuartzSchedulerTypedExtension = QuartzSchedulerTypedExtension(typedSystem)
  override val firstFire: Date = scheduler.createTypedJobSchedule("import-job", receiver = importerActorRef,
    Importer.DownloadFile(source),cronExpression = schedule, timezone = TimeZone.getTimeZone("America/Los_Angeles"))
}
