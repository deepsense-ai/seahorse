/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager.service.actors

import scala.concurrent.Future

import akka.actor.Actor
import akka.pattern.pipe
import com.google.inject.Inject
import org.joda.time.DateTime

import io.deepsense.commons.models.Id
import io.deepsense.commons.utils.Logging
import io.deepsense.sessionmanager.service.EventStore.Event
import io.deepsense.sessionmanager.service.actors.SessionServiceActor._
import io.deepsense.sessionmanager.service.executor.SessionExecutorClients
import io.deepsense.sessionmanager.service.livy.Livy
import io.deepsense.sessionmanager.service.{EventStore, Session, StatusInferencer}
import io.deepsense.workflowexecutor.communication.message.global.Heartbeat

class SessionServiceActor @Inject()(
  private val livyClient: Livy,
  private val eventStore: EventStore,
  private val statusInferencer: StatusInferencer,
  private val sessionExecutorClients: SessionExecutorClients
) extends Actor with Logging {

  import scala.concurrent.ExecutionContext.Implicits.global

  override def receive: Receive = {
    case r: Request => handleRequest(r)
    case h: Heartbeat => handleHeartbeat(h)
    case x => unhandled(x)
  }

  private def handleRequest(request: Request): Unit = {
    request match {
      case GetRequest(id) =>
        handleGet(id) pipeTo sender()
      case KillRequest(id) =>
        handleKill(id) pipeTo sender()
      case ListRequest() =>
        handleList() pipeTo sender()
      case CreateRequest(id) =>
        handleCreate(id) pipeTo sender()
    }
  }

  private def handleHeartbeat(heartbeat: Heartbeat): Unit = {
    val workflowId: Id = heartbeat.sessionId
    eventStore.heartbeat(workflowId).foreach {
      case Left(_) =>
        // Invalid WorkflowId
        logger.warn(s"Received incorrect heartbeat from $workflowId. Sending PoisonPill")
        sessionExecutorClients.sendPoisonPill(workflowId)
      case Right(_) =>
        // All's good!
    }
  }

  private def handleGet(id: Id): Future[Option[Session]] = {
    val singleEventToSession = eventToSession(id, _: Event)
    eventStore.getLastEvent(id).map(_.map(singleEventToSession))
  }

  private def eventToSession(workflowId: Id, event: Event): Session = {
    val status = statusInferencer.statusFromEvent(event, DateTime.now)
    Session(workflowId, status)
  }

  private def handleList(): Future[Seq[Session]] = {
    eventStore.getLastEvents.map(_.map {
      case (workflowId, event) => eventToSession(workflowId, event)
    }.toSeq)
  }

  private def handleCreate(id: Id): Future[Id] = {
    eventStore.started(id).flatMap {
      case Left(_) => Future.successful(id)
      case Right(_) => livyClient.createSession(id).map(_ => id)
    }
  }

  private def handleKill(workflowId: Id): Future[Unit] = {
    sessionExecutorClients.sendPoisonPill(workflowId)
    eventStore.killed(workflowId)
  }
}

object SessionServiceActor {
  sealed trait Request
  case class GetRequest(id: Id) extends Request
  case class KillRequest(id: Id) extends Request
  case class ListRequest() extends Request
  case class CreateRequest(workflowId: Id) extends Request
}
