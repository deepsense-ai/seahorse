/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.seahorse.scheduling.converters

import java.util.UUID

import io.deepsense.seahorse.scheduling.db.schema.WorkflowScheduleSchema.WorkflowScheduleDB
import io.deepsense.seahorse.scheduling.model.{Schedule, WorkflowExecutionInfo, WorkflowSchedule}

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
