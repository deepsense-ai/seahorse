/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager.service.statusinferencer

import scala.concurrent.duration._

import com.google.inject.Inject
import com.google.inject.name.Named
import org.joda.time.DateTime

import io.deepsense.commons.utils.Logging
import io.deepsense.sessionmanager.service.EventStore.{Event, HeartbeatReceived, Started}
import io.deepsense.sessionmanager.service.{Status, StatusInferencer}

class DefaultStatusInferencer @Inject()(
  @Named("status-inference.heartbeat.interval") heartbeatInterval: Int,
  @Named("status-inference.heartbeat.missing-count") missingHeartbeatCount: Int,
  @Named("status-inference.startup.duration") startupDuration: Int
) extends StatusInferencer with Logging {

  private val startingMaxDuration = startupDuration.seconds
  private val heartbeatMaxInterval = (heartbeatInterval * missingHeartbeatCount).seconds

  override def statusFromEvent(event: Event, at: DateTime): Status.Value = {
    event match {
      case s: Started => check(s, at, startingMaxDuration, Status.Creating)
      case h: HeartbeatReceived => check(h, at, heartbeatMaxInterval, Status.Running)
    }
  }

  private def check(
      event: Event,
      at: DateTime,
      constraint: Duration,
      okStatus: Status.Value): Status.Value = {
    val status = if (duration(event.happenedAt, at) < constraint) {
      okStatus
    } else {
      Status.Error
    }
    logger.info(s"Status for '$event' at '$at' is: '$status'")
    status
  }

  private def duration(from: DateTime, to: DateTime): Duration =
    Math.abs(to.getMillis - from.getMillis).milliseconds
}
