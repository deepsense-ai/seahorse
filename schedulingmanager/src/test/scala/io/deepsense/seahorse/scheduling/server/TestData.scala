/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.seahorse.scheduling.server

import java.util.UUID

import io.deepsense.seahorse.scheduling.model._

object TestData {

  def someScheduleFor(workflowId: UUID) = WorkflowSchedule(
    UUID.randomUUID(),
    schedule = Schedule("* * * * *"),
    workflowId = workflowId,
    executionInfo = WorkflowExecutionInfo("some_email@test.test", 2)
  )

}
