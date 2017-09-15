/**
 * Copyright 2016 deepsense.ai (CodiLime, Inc)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ai.deepsense.seahorse.scheduling.api

import java.util.UUID

import slick.dbio._

import scala.concurrent.Await
import scala.util.{Failure, Success, Try}

import ai.deepsense.commons.config.ConfigToPropsLossy
import ai.deepsense.commons.service.api.CommonApiExceptions
import ai.deepsense.commons.service.db.dbio.{GenericDBIOs, TryDBIO}
import ai.deepsense.seahorse.scheduling.SchedulingManagerConfig
import ai.deepsense.seahorse.scheduling.converters.SchedulesConverters
import ai.deepsense.seahorse.scheduling.db.Database
import ai.deepsense.seahorse.scheduling.db.dbio.WorkflowSchedulesDBIOs
import ai.deepsense.seahorse.scheduling.db.schema.WorkflowScheduleSchema
import ai.deepsense.seahorse.scheduling.db.schema.WorkflowScheduleSchema.WorkflowScheduleDB
import ai.deepsense.seahorse.scheduling.model.{JsonBodyForError, WorkflowSchedule}
import ai.deepsense.seahorse.scheduling.schedule.{RunWorkflowJob, WorkflowScheduler}

class SchedulingManagerApi extends DefaultApi {
  import scala.concurrent.ExecutionContext.Implicits.global

  private val scheduler = {
    val s = new WorkflowScheduler[RunWorkflowJob](ConfigToPropsLossy(SchedulingManagerConfig.config))
    s.start()
    s
  }

  private val genericDBIOs = new GenericDBIOs[WorkflowSchedule, WorkflowScheduleDB] {
    override val api = ai.deepsense.seahorse.scheduling.db.Database.api
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
      import ai.deepsense.seahorse.scheduling.db.Database.api.{Database => _, _}
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
