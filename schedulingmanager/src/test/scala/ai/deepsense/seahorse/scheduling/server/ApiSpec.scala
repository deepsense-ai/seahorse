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

package ai.deepsense.seahorse.scheduling.server

import java.util.UUID

import org.scalatest.{FreeSpec, Matchers}

import ai.deepsense.seahorse.scheduling.api.{ApiException, SchedulingManagerApi}
import ai.deepsense.seahorse.scheduling.db.FlywayMigration

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
