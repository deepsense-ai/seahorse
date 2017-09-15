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

package ai.deepsense.seahorse.scheduling.converters

import java.util.UUID

import ai.deepsense.seahorse.scheduling.db.schema.WorkflowScheduleSchema.WorkflowScheduleDB
import ai.deepsense.seahorse.scheduling.model.{Schedule, WorkflowExecutionInfo, WorkflowSchedule}

object SchedulesConverters {

  def fromApi(schedule: WorkflowSchedule): WorkflowScheduleDB = WorkflowScheduleDB(
    id = schedule.id,
    cron = schedule.schedule.cron,
    workflowId = schedule.workflowId,
    emailForReports = schedule.executionInfo.emailForReports,
    presetId = schedule.executionInfo.presetId
  )

  def fromDb(schedule: WorkflowScheduleDB) = WorkflowSchedule(
    id = schedule.id,
    schedule = Schedule(
      cron = schedule.cron
    ),
    workflowId = schedule.workflowId,
    executionInfo = WorkflowExecutionInfo(
      emailForReports = schedule.emailForReports,
      presetId = schedule.presetId
    )
  )

}
