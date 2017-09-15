/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager.service.eventstore

import scala.collection.mutable
import scala.concurrent.Future
import org.joda.time.{DateTime, DateTimeZone}
import io.deepsense.commons.models.Id
import io.deepsense.sessionmanager.rest.requests.ClusterDetails
import io.deepsense.sessionmanager.service.EventStore
import io.deepsense.sessionmanager.service.EventStore.{HeartbeatReceived, InvalidWorkflowId, Started, _}

/**
  * Collects all Executor's events (starting, killing and receiving heartbeat).
  *
  * WARNING: This class is not thread safe. (But it does not have to be as it
  * can be enclosed within an Actor.)
  */
class InMemoryEventStore extends EventStore {
  val events = mutable.Map[Id, Event]()

  override def getLastEvent(workflowId: Id): Future[Option[Event]] =
    Future.successful(events.get(workflowId))

  override def getLastEvents: Future[Map[Id, Event]] = Future.successful(events.toMap)

  override def heartbeat(workflowId: Id): Future[Either[InvalidWorkflowId, Unit]] =
    Future.successful {
      val previousEvent = events.get(workflowId)
      previousEvent match {
        case Some(event) =>
          events.put(workflowId, HeartbeatReceived(workflowId, utcNow, event.cluster))
          Right(Unit)
        case None =>
          Left(InvalidWorkflowId())
      }
    }

  override def started(
      workflowId: Id,
      clusterDetails: ClusterDetails)
      : Future[Either[SessionExists, Unit]] = Future.successful {
    def createSession: Started = {
      val startedEvent = Started(workflowId, utcNow, clusterDetails)
      events.put(workflowId, startedEvent)
      startedEvent
    }

    events.get(workflowId) match {
      case Some(event) =>
        Left(SessionExists())
      case None =>
        createSession
        Right(Unit)
    }
  }

  override def killed(workflowId: Id): Future[Unit] = Future.successful(events.remove(workflowId))

  private def utcNow: DateTime = DateTime.now(DateTimeZone.UTC)
}
