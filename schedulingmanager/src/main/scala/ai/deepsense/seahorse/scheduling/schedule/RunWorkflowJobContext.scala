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

package ai.deepsense.seahorse.scheduling.schedule

import java.util.UUID

import akka.actor.ActorSystem

import ai.deepsense.seahorse.scheduling.SchedulingManagerConfig

private[schedule] object RunWorkflowJobContext {
  private[this] val config = SchedulingManagerConfig.config

  val actorSystem = ActorSystem("scheduling-manager-run-workflow-job", config)

  val userId = UUID.fromString(config.getString("predefined-users.scheduler.id"))
  val userName = config.getString("predefined-users.scheduler.name")

  def generateWorkflowUrl(uuid: UUID): String =
    config.getString("scheduling-manager.workflowUrlPattern").format(uuid.toString)
}
