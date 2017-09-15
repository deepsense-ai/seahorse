/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.seahorse.scheduling.server

import java.util.UUID

import org.scalatest.{FreeSpec, Matchers}

import io.deepsense.seahorse.scheduling.api.{ApiException, SchedulingManagerApi}
import io.deepsense.seahorse.scheduling.db.FlywayMigration

class ApiSpec extends FreeSpec with Matchers {

  // TODO Copy pasta test

  private lazy val api = {
    FlywayMigration.run()
    new SchedulingManagerApi()
  }

  "Api consumer" - {
    "can add new workflow schedules" in {
      val workflowId = UUID.randomUUID()
      val scheduling = TestData.someScheduleFor(workflowId)
      api.putWorkflowScheduleImpl(scheduling.id, scheduling)
      api.getWorkflowSchedulesImpl() should contain(scheduling)
      api.getWorkflowScheduleImpl(scheduling.id) shouldEqual scheduling

      info("Add operation is idempotent")
      api.putWorkflowScheduleImpl(scheduling.id, scheduling)
      api.getWorkflowSchedulesImpl() should contain(scheduling)
      api.getWorkflowScheduleImpl(scheduling.id) shouldEqual scheduling

      info("Schedulings might be fetched by workflow id")
      val scheduleForOtherWorkflow = TestData.someScheduleFor(UUID.randomUUID())
      api.putWorkflowScheduleImpl(scheduleForOtherWorkflow.id, scheduleForOtherWorkflow)

      val anotherSchedulingForSameWorkflow = TestData.someScheduleFor(workflowId)
      api.putWorkflowScheduleImpl(anotherSchedulingForSameWorkflow.id, anotherSchedulingForSameWorkflow)

      api.getSchedulesForWorkflowImpl(workflowId) should contain allOf(
        scheduling, anotherSchedulingForSameWorkflow
        )
      api.getSchedulesForWorkflowImpl(workflowId) should not contain scheduleForOtherWorkflow

      info("Workflow schedule can also be deleted")
      api.deleteWorkflowScheduleImpl(scheduling.id)
      api.getWorkflowSchedulesImpl() shouldNot contain(scheduling)
      the[ApiException].thrownBy(
        api.getWorkflowScheduleImpl(scheduling.id)
      ).errorCode shouldBe 404
    }

  }
}
