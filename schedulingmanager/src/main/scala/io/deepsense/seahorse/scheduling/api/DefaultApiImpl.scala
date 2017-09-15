/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.seahorse.scheduling.api

import java.util.UUID

import io.deepsense.seahorse.scheduling.model.{JsonBodyForError, WorkflowSchedule}

class DefaultApiImpl extends DefaultApi {

  override def deleteWorkflowScheduleImpl(scheduleId: UUID): Unit = ???

  override def getSchedulesForWorkflowImpl(workflowId: UUID): List[WorkflowSchedule] = ???

  override def getWorkflowScheduleImpl(scheduleId: UUID): WorkflowSchedule = ???

  override def getWorkflowSchedulesImpl(): List[WorkflowSchedule] = ???

  override def putWorkflowScheduleImpl(scheduleId: UUID, workflowSchedule: WorkflowSchedule): WorkflowSchedule = ???

  // Codegen abstracts from application-specific error body format
  override protected def formatErrorBody(code: Int, msg: String): String = JsonBodyForError(code, msg)
}
