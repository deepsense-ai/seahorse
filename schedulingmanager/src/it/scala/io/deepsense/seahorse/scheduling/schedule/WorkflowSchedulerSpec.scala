/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.seahorse.scheduling.schedule

import java.util.UUID

import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.Future

import io.deepsense.commons.config.ConfigToPropsLossy
import io.deepsense.seahorse.scheduling.SchedulingManagerConfig
import io.deepsense.seahorse.scheduling.db.FlywayMigration
import io.deepsense.seahorse.scheduling.model.{Schedule, WorkflowExecutionInfo, WorkflowSchedule}

class WorkflowSchedulerSpec extends WordSpec with Matchers {
  "WorkflowScheduler" should {
    "schedule and unschedule workflow execution" in {
      FlywayMigration.run()

      val scheduler = new WorkflowScheduler[TestJob](configAsProperties)
      scheduler.start()

      scheduler.activateSchedule(everyMinuteSchedule)
      info("For two minutes")
      Thread.sleep(120000)
      scheduler.deactivateSchedule(everyMinuteSchedule.id)
      val counterAfterTwoMinutes = TestJob.executions.size

      info("Schedule execution every minute")
      counterAfterTwoMinutes should be <= 3
      counterAfterTwoMinutes should be >= 2

      info("Not scheduled after being unscheduled")
      Thread.sleep(60000)
      TestJob.executions.size shouldEqual counterAfterTwoMinutes

      info("Workflow id and email match those of schedule")
      TestJob.executions.foreach { case (executionWorkflowId, executionEmail, executionPresetId) =>
        executionWorkflowId shouldEqual workflowId
        executionEmail shouldEqual email
        executionPresetId shouldEqual presetId
      }
    }
    "not blow up if a workflow is unscheduled twice" in {
      FlywayMigration.run()

      val scheduler = new WorkflowScheduler[TestJob](configAsProperties)
      scheduler.start()
      scheduler.activateSchedule(everyMinuteSchedule)
      scheduler.deactivateSchedule(everyMinuteSchedule.id)
      scheduler.deactivateSchedule(everyMinuteSchedule.id)
    }
  }
  private[this] val scheduleId = "31bcb49d-fad5-4ab7-aca4-f2b8fb8ccca9"
  private[this] val workflowId = "fb36193c-7b8a-4b89-91ff-5c85f5bbd79e"
  private[this] val email = "foo@deepsense.io"
  private[this] val presetId = 22
  private[this] val everyMinute = "* * * * *"
  private[this] val everyMinuteSchedule = WorkflowSchedule(
    UUID.fromString(scheduleId),
    Schedule(everyMinute),
    UUID.fromString(workflowId),
    WorkflowExecutionInfo(email, presetId))
  private[this] val configAsProperties = ConfigToPropsLossy(SchedulingManagerConfig.config)
}

// This class needs to be in global scope, otherwise it isn't instantiated in Quartz, hence counter is also global
private[scheduling] class TestJob extends WorkflowJob {
  override def runWorkflow(workflowId: String, email: String, presetId: Long): Future[Unit] = {
    TestJob.executions = TestJob.executions :+ (workflowId, email, presetId)
    Future.successful(())
  }
}
private[scheduling] object TestJob {
  var executions: Seq[(String, String, Long)] = Seq.empty
}

