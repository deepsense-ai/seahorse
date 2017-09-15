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

package ai.deepsense.sessionmanager.service.sessionspawner

import java.time.Instant

import com.google.inject.Inject
import com.google.inject.name.Named
import org.apache.spark.launcher.SparkAppHandle

import ai.deepsense.sessionmanager.service.Status
import ai.deepsense.sessionmanager.service.Status.Status

trait StateInferencer {
  def handleHeartbeat(currentTime: Instant): StateInferencer
  def statusForApi(currentTime: Instant, sparkState: SparkAppHandle.State): Status
}

// Factory is needed because of config and guice
class StateInferencerFactory @Inject()(
    @Named("session-manager.executor-first-heartbeat-timeout") val firstHeartbeatTimeout: Int,
    @Named("session-manager.heartbeat-maximum-delay") val heartbeatMaximumDelay: Int
) {

  def newInferencer(startTime: Instant): StateInferencer =
    StateInferencerForClientMode(startTime, None)

  private case class StateInferencerForClientMode(
      startTime: Instant, lastHeartbeat: Option[Instant]
  ) extends StateInferencer {

    def handleHeartbeat(currentTime: Instant): StateInferencer = this.copy(
      lastHeartbeat = Some(currentTime)
    )

    def statusForApi(currentTime: Instant, sparkState: SparkAppHandle.State): Status = {
      val executorIsNotRunning = sparkState.isFinal

      if(executorIsNotRunning) {
        Status.Error
      } else {
        lastHeartbeat match {
          case None => statusBeforeFirstHeartbeat(currentTime)
          case Some(lastHeartbeatTime) => statusAfterFirstHeartbeat(currentTime, lastHeartbeatTime)
        }
      }
    }

    private def statusAfterFirstHeartbeat(currentTime: Instant, lastHeartbeatTime: Instant) = {
      val secondsFromLastHeartbeat = secondsBetween(currentTime, lastHeartbeatTime)
      if (secondsFromLastHeartbeat < heartbeatMaximumDelay) {
        Status.Running
      } else {
        Status.Error
      }
    }

    private def statusBeforeFirstHeartbeat(currentTime: Instant) = {
      val secondsFromStart = secondsBetween(currentTime, startTime)
      if (secondsFromStart < firstHeartbeatTimeout) {
        Status.Creating
      } else {
        Status.Error
      }
    }

    private def secondsBetween(a: Instant, b: Instant) =
      java.time.Duration.between(a, b).abs().getSeconds.toInt

  }

}


