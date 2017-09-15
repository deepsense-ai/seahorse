/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.seahorse.scheduling.schedule

import java.util.{Properties, UUID}

import com.cronutils.mapper.CronMapper
import com.cronutils.model.CronType
import com.cronutils.model.definition.CronDefinitionBuilder
import com.cronutils.parser.CronParser
import org.quartz._
import org.quartz.impl.StdSchedulerFactory

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.reflect.{ClassTag, classTag}

import io.deepsense.commons.utils.LoggerForCallerClass
import io.deepsense.seahorse.scheduling.api.SchedulerApiExceptions
import io.deepsense.seahorse.scheduling.model.WorkflowSchedule

abstract class WorkflowJob extends Job {
  val logger = LoggerForCallerClass()

  override final def execute(context: JobExecutionContext): Unit = {
    val jobInfo = context.getJobDetail.getJobDataMap
    val workflowId = jobInfo.getString(WorkflowJob.workflowIdKey)
    val email = jobInfo.getString(WorkflowJob.emailKey)
    val presetId = jobInfo.getLong(WorkflowJob.presetIdKey)
    logger.debug(s"Starting scheduled execution of workflow $workflowId for $email and preset $presetId.")
    Await.result(runWorkflow(workflowId, email, presetId), Duration.Inf)
  }

  def runWorkflow(workflowId: String, sendReportToEmail: String, presetId: Long): Future[Unit]
}

object WorkflowJob {
  val presetIdKey = "presetId"
  val workflowIdKey = "workflowId"
  val emailKey = "email"
}

class WorkflowScheduler[JobType <: Job : ClassTag](properties: Properties) {
  import WorkflowScheduler._

  val logger = LoggerForCallerClass()

  // Starts the Quartz service; all previously saved schedules will still be scheduled.
  def start(): Unit = {
    quartz.start()
  }

  def stop(): Unit = {
    quartz.shutdown(true)
  }

  def activateSchedule(workflowSchedule: WorkflowSchedule): Unit = wrapSchedulerErrors {
    if (isScheduled(workflowSchedule)) {
      deactivateSchedule(workflowSchedule.id)
    }
    activateNonExistingSchedule(workflowSchedule)
  }

  def deactivateSchedule(scheduleId: UUID): Unit = wrapSchedulerErrors {
    quartz.deleteJob(jobKey(scheduleId))
    logger.info(s"Unscheduled workflow schedule with id $scheduleId.")
  }

  private def isScheduled(workflowSchedule: WorkflowSchedule): Boolean = {
    quartz.getJobDetail(jobKey(workflowSchedule)) != null
  }

  private def activateNonExistingSchedule(workflowSchedule: WorkflowSchedule): Unit = {
    val cronForQuartz = standardToQuartzCron(workflowSchedule.schedule.cron)
    val jobDetail = JobBuilder
      .newJob(classTag[JobType].runtimeClass.asInstanceOf[Class[JobType]])
      .withIdentity(jobKey(workflowSchedule))
      .usingJobData(WorkflowJob.workflowIdKey, workflowSchedule.workflowId.toString)
      .usingJobData(WorkflowJob.emailKey, workflowSchedule.executionInfo.emailForReports)
      .usingJobData(WorkflowJob.presetIdKey, long2Long(workflowSchedule.executionInfo.presetId))
      .build()
    val trigger = TriggerBuilder
      .newTrigger()
      .withIdentity(triggerKey(workflowSchedule))
      .forJob(jobDetail)
      .withSchedule(CronScheduleBuilder.cronSchedule(cronForQuartz))
      .build()
    quartz.scheduleJob(jobDetail, trigger)
    logger.info(s"Scheduled $workflowSchedule with Quartz CRON string '$cronForQuartz'.")
  }

  private def wrapSchedulerErrors[T](action: T): T = {
    try {
      action
    } catch { case exception: SchedulerException =>
      logger.error(exception.toString)
      throw SchedulerApiExceptions.schedulerError
    }
  }

  private[this] val quartz: Scheduler = {
    val factory = new StdSchedulerFactory
    factory.initialize(properties)
    val p = properties
    factory.getScheduler
  }
}

object WorkflowScheduler {

  private def jobKey(workflowSchedule: WorkflowSchedule): JobKey = jobKey(workflowSchedule.id)
  private def jobKey(scheduleId: UUID): JobKey =
    JobKey.jobKey(scheduleId.toString, workflowJobsGroup)
  private def triggerKey(workflowSchedule: WorkflowSchedule): TriggerKey =
    TriggerKey.triggerKey(workflowSchedule.id.toString, workflowTriggersGroup)


  // Cron for Quartz differs significantly from CRON standard in at least these three aspects:
  // - Quartz forces that one of "day of week" or "day of month" is "?", which excludes even such expression as
  //   "* * * * *"
  // - Quartz has non-standard seconds as the first part.
  // - Quartz uses 1-based weekday numeration, contrary to standard 0-6.
  // All three are taken care of by CronMapper.
  private def standardToQuartzCron(standardCron: String): String = {
    val unixParser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX))
    val cron = unixParser.parse(standardCron)
    CronMapper.fromUnixToQuartz().map(cron).asString()

  }

  private val workflowJobsGroup = "workflowJob"
  private val workflowTriggersGroup = "workflowTrigger"

}
