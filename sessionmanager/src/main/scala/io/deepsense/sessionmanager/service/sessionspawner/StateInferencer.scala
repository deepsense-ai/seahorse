/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager.service.sessionspawner

import java.time.Instant

import com.google.inject.Inject
import com.google.inject.name.Named
import org.apache.spark.launcher.SparkAppHandle

import io.deepsense.sessionmanager.service.Status
import io.deepsense.sessionmanager.service.Status.Status

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


