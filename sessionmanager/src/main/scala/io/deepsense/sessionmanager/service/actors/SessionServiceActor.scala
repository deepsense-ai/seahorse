/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager.service.actors

import scala.concurrent.Future

import akka.actor.Actor
import akka.agent.Agent
import akka.pattern.pipe
import com.google.inject.Inject
import org.apache.spark.launcher.SparkAppHandle
import org.joda.time.DateTime

import io.deepsense.commons.models.Id
import io.deepsense.commons.utils.Logging
import io.deepsense.commons.models.ClusterDetails
import io.deepsense.sessionmanager.service.EventStore.Event
import io.deepsense.sessionmanager.service.actors.SessionServiceActor._
import io.deepsense.sessionmanager.service.executor.SessionExecutorClients
import io.deepsense.sessionmanager.service.sessionspawner.{SessionConfig, SessionSpawner}
import io.deepsense.sessionmanager.service.{EventStore, Session, StatusInferencer}
import io.deepsense.workflowexecutor.communication.message.global.Heartbeat

class SessionServiceActor @Inject()(
  private val sessionSpawner: SessionSpawner,
  private val eventStore: EventStore,
  private val statusInferencer: StatusInferencer,
  private val sessionExecutorClients: SessionExecutorClients
) extends Actor with Logging {

  import scala.concurrent.ExecutionContext.Implicits.global

  private val sparkHandleBySessionId = Agent(Map.empty[Id, SparkAppHandle])

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
      case CreateRequest(sessionConfig, clusterConfig) =>
        handleCreate(sessionConfig, clusterConfig) pipeTo sender()
    }
  }

  private def handleHeartbeat(heartbeat: Heartbeat): Unit = {
    logger.trace(s"Received Heartbeat $heartbeat")
    val workflowId: Id = heartbeat.workflowId
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
    logger.info(s"Session '$workflowId' is '$status'")
    Session(workflowId, status, event.cluster)
  }

  private def handleList(): Future[Seq[Session]] = {
    eventStore.getLastEvents.map(_.map {
      case (workflowId, event) => eventToSession(workflowId, event)
    }.toSeq)
  }

  private def handleCreate(
      sessionConfig: SessionConfig,
      clusterConfig: ClusterDetails): Future[Id] = {
    eventStore.started(sessionConfig.workflowId, clusterConfig).flatMap {
      case Left(_) =>
        logger.info(s"Session '${sessionConfig.workflowId}' already exists!")
        Future.successful(sessionConfig.workflowId)
      case Right(_) =>
        logger.info(s"Session '${sessionConfig.workflowId}' does not exist. Creating!")
        val handleValidation = sessionSpawner.createSession(sessionConfig, clusterConfig)
        // TODO We already know that there was an error with launching App. We ignore it for now
        handleValidation.foreach(handle => {
          sparkHandleBySessionId.send(_.updated(sessionConfig.workflowId, handle))
        })
        Future.successful(sessionConfig.workflowId)
    }
  }

  private def handleKill(workflowId: Id): Future[Unit] = {
    tryToKillInstantlyIfHandleAvailable(workflowId)
    sessionExecutorClients.sendPoisonPill(workflowId)
    eventStore.killed(workflowId)
  }

  private def tryToKillInstantlyIfHandleAvailable(workflowId: Id): Unit = {
    sparkHandleBySessionId.send { handleBySessionId =>
      handleBySessionId.get(workflowId).foreach(_.kill())
      handleBySessionId - workflowId
    }
  }
}

object SessionServiceActor {
  sealed trait Request
  case class GetRequest(id: Id) extends Request
  case class KillRequest(id: Id) extends Request
  case class ListRequest() extends Request
  case class CreateRequest(
    sessionConfig: SessionConfig,
    clusterConfig: ClusterDetails
  ) extends Request
}
