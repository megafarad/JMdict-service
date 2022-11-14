package com.megafarad.jmdictsvc.component.impl

import akka.actor.typed.scaladsl.adapter._
import com.megafarad.jmdictsvc.component._
import com.megafarad.jmdictsvc.job.ImportJob
import com.megafarad.jmdictsvc.utils.Logging
import org.quartz
import org.quartz.JobBuilder._
import org.quartz.TriggerBuilder._
import org.quartz.CronScheduleBuilder._
import org.quartz.core.jmx.JobDataMapSupport
import org.quartz.impl.StdSchedulerFactory
import org.quartz.{CronExpression, CronTrigger, JobDataMap, JobDetail}

import java.util.{Date, TimeZone}
import scala.jdk.CollectionConverters._

trait ImporterComponentImpl extends ImporterComponent {
  this: ActorSystemComponent with DatabaseComponent with ConfigComponent with Repositories with Logging =>
  private val schedule = config.getString("importer.schedule")
  private val source = config.getString("importer.source")
  private val timeZone = config.getString("importer.timezone")
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
  val job: JobDetail = newJob(classOf[ImportJob])
    .withIdentity("ImportJob")
    .usingJobData(jobData)
    .build()

  override val firstFire: Date = scheduler.scheduleJob(job, trigger)

}
