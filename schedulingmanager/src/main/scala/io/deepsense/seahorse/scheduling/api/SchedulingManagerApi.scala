/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.seahorse.scheduling.api

import java.util.UUID

import slick.dbio._

import scala.concurrent.Await
import scala.util.{Failure, Success, Try}

import io.deepsense.commons.config.ConfigToPropsLossy
import io.deepsense.commons.service.api.CommonApiExceptions
import io.deepsense.commons.service.db.dbio.{GenericDBIOs, TryDBIO}
import io.deepsense.seahorse.scheduling.SchedulingManagerConfig
import io.deepsense.seahorse.scheduling.converters.SchedulesConverters
import io.deepsense.seahorse.scheduling.db.Database
import io.deepsense.seahorse.scheduling.db.dbio.WorkflowSchedulesDBIOs
import io.deepsense.seahorse.scheduling.db.schema.WorkflowScheduleSchema
import io.deepsense.seahorse.scheduling.db.schema.WorkflowScheduleSchema.WorkflowScheduleDB
import io.deepsense.seahorse.scheduling.model.{JsonBodyForError, WorkflowSchedule}
import io.deepsense.seahorse.scheduling.schedule.{RunWorkflowJob, WorkflowScheduler}

class SchedulingManagerApi extends DefaultApi {
  import scala.concurrent.ExecutionContext.Implicits.global

  private val scheduler = {
    val s = new WorkflowScheduler[RunWorkflowJob](ConfigToPropsLossy(SchedulingManagerConfig.config))
    s.start()
    s
  }

  private val genericDBIOs = new GenericDBIOs[WorkflowSchedule, WorkflowScheduleDB] {
    override val api = io.deepsense.seahorse.scheduling.db.Database.api
    override val table = WorkflowScheduleSchema.workflowScheduleTable
    override val fromDB = SchedulesConverters.fromDb _
    override val fromApi = SchedulesConverters.fromApi _
  }

  override def getSchedulesForWorkflowImpl(workflowId: UUID): List[WorkflowSchedule] =
    WorkflowSchedulesDBIOs.getAllForWorkflow(workflowId).run()

  override def getWorkflowScheduleImpl(scheduleId: UUID): WorkflowSchedule =
    genericDBIOs.get(scheduleId).run()

  override def getWorkflowSchedulesImpl(): List[WorkflowSchedule] =
    genericDBIOs.getAll.run()

  override def putWorkflowScheduleImpl(scheduleId: UUID, workflowSchedule: WorkflowSchedule): WorkflowSchedule = (for {
    updated <- genericDBIOs.insertOrUpdate(scheduleId, workflowSchedule)
    () <- TryDBIO(scheduler.activateSchedule(updated))
  } yield updated).run()

  override def deleteWorkflowScheduleImpl(scheduleId: UUID): Unit = (for {
    () <- genericDBIOs.delete(scheduleId)
    () <- TryDBIO(scheduler.deactivateSchedule(scheduleId))
  } yield ()).run()

  // TODO DRY
  implicit class DBIOOps[T](dbio: DBIO[T]) {
    import scala.concurrent.duration._
    def run(): T = {
      import io.deepsense.seahorse.scheduling.db.Database.api.{Database => _, _}
      val futureResult = Database.db.run(dbio.transactionally)
      Try {
        Await.result(futureResult, SchedulingManagerConfig.database.timeout)
      } match {
        case Success(value) => value
        case Failure(commonEx: CommonApiExceptions.ApiException) => throw ApiExceptionFromCommon(commonEx)
      }
    }
    implicit def durationJavaToScala(d: java.time.Duration): Duration = Duration.fromNanos(d.toNanos)
  }

  // Codegen abstracts from application-specific error body format
  override protected def formatErrorBody(code: Int, msg: String): String = JsonBodyForError(code, msg)
}
