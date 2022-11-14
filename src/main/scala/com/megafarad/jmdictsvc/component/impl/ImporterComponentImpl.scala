package com.megafarad.jmdictsvc.component.impl

import akka.actor.typed.scaladsl.adapter._
import com.megafarad.jmdictsvc.component._
import com.megafarad.jmdictsvc.job.ImportJob
import com.megafarad.jmdictsvc.utils.Logging
import org.quartz
import org.quartz.JobBuilder._
import org.quartz.TriggerBuilder._
import org.quartz.CronScheduleBuilder._
import org.quartz.SimpleScheduleBuilder._
import org.quartz.core.jmx.JobDataMapSupport
import org.quartz.impl.StdSchedulerFactory
import org.quartz.{CronExpression, CronTrigger, JobBuilder, JobDataMap, JobDetail}

import java.util.{Date, TimeZone}
import scala.jdk.CollectionConverters._

trait ImporterComponentImpl extends ImporterComponent {
  this: ActorSystemComponent with DatabaseComponent with ConfigComponent with Repositories with Logging =>
  private val schedule = config.getString("importer.schedule")
  private val source = config.getString("importer.source")
  private val timeZone = config.getString("importer.timezone")
  private val importOnStartup = config.getBoolean("importer.importOnStartup")
  private val typedSystem = actorSystem.toTyped
  private val jobDataMap = Map[String, AnyRef](
      "actorSystem" -> typedSystem,
      "entryRepository" -> entryRepository,
      "source" -> source,
      "logger" -> log
  )



  override val scheduler: quartz.Scheduler = StdSchedulerFactory.getDefaultScheduler
  scheduler.start()

  val expression = new CronExpression(schedule)
  val trigger: CronTrigger = newTrigger()
    .withIdentity("ImportTrigger")
    .withSchedule(cronSchedule(expression).inTimeZone(TimeZone.getTimeZone(timeZone)))
    .build()
  val jobData: JobDataMap = JobDataMapSupport.newJobDataMap(jobDataMap.asJava)
  val jobBuilder: JobBuilder = newJob(classOf[ImportJob])
    .usingJobData(jobData)

  override val firstFire: Date =
  if (importOnStartup) {
    scheduler.scheduleJob(jobBuilder.withIdentity("ImportJob").build(), trigger)
    val firstFire = new Date(System.currentTimeMillis() + 30 * 1000L)
    scheduler.scheduleJob(jobBuilder.withIdentity("ImportOnStartupJob").build(), newTrigger()
      .withIdentity("ImportOnStartup").startAt(firstFire).build())
  } else scheduler.scheduleJob(jobBuilder.withIdentity("ImportJob").build(), trigger)
  log.info("Scheduled import, first fire: {}", firstFire)

}
