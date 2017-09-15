/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager.service.actors

import scala.collection.mutable
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}

import akka.actor.Actor
import com.google.inject.Inject
import com.google.inject.name.Named

import io.deepsense.commons.models.ClusterDetails
import io.deepsense.commons.utils.Logging
import io.deepsense.deeplang.Model.Id
import io.deepsense.sessionmanager.service.Session
import io.deepsense.sessionmanager.service.actors.SessionServiceActor._
import io.deepsense.sessionmanager.service.executor.SessionExecutorClients
import io.deepsense.sessionmanager.service.sessionspawner.{ExecutorSession, SessionConfig, SessionSpawner}
import io.deepsense.workflowexecutor.communication.message.global.Heartbeat

class SessionServiceActor @Inject()(
  private val sessionSpawner: SessionSpawner,
  private val sessionExecutorClients: SessionExecutorClients
) extends Actor with Logging {

  // NOTE! No synchronising is needed because all mutations are in `receive` method call scope.
  // When modified from outside `receive` (possible throught futures!) scope add synchronization
  val sessionStateByWorkflowId = mutable.Map.empty[Id, ExecutorSession]

  override def receive: Receive = {
    case r: Request => handleRequest(r)
    case h: Heartbeat => handleHeartbeat(h)
    case x => unhandled(x)
  }

  private def handleRequest(request: Request): Unit = {
    request match {
      case GetRequest(id) => sender() ! handleGet(id)
      case KillRequest(id) => sender() ! handleKill(id)
      case ListRequest() => sender() ! handleList()
      case LaunchRequest(id) => sender() ! handleLaunch(id)
      case CreateRequest(sessionConfig, clusterConfig) =>
        sender() ! handleCreate(sessionConfig, clusterConfig)
    }
  }

  private def handleLaunch(id: Id): Try[Unit] = {
    if (sessionStateByWorkflowId.contains(id)) {
      Success(sessionExecutorClients.launchWorkflow(id))
    } else {
      Failure(new IllegalArgumentException(s"Session for the given id not found: $id"))
    }
  }

  private def handleCreate(
      sessionConfig: SessionConfig,
      clusterDetails: ClusterDetails): Session = {
    val workflowId = sessionConfig.workflowId
    val session = sessionStateByWorkflowId.get(workflowId) match {
      case Some(existingSession) =>
        logger.warn(s"Session id=$workflowId already exists. Ignoring in the sake of idempotency.")
        existingSession
      case None => sessionSpawner.createSession(sessionConfig, clusterDetails)
    }
    sessionStateByWorkflowId(workflowId) = session
    session.sessionForApi()
  }

  private def handleHeartbeat(heartbeat: Heartbeat): Unit = {
    val workflowId = heartbeat.workflowId
    logger.debug(s"Session id=$workflowId received heartbeat $heartbeat")

    sessionStateByWorkflowId.get(workflowId) match {
      case Some(session) =>
        val updatedSession = session.handleHeartbeat()
        sessionStateByWorkflowId(workflowId) = updatedSession
      case None =>
        logger.error(
          s"""Session id=$workflowId unknown!
             |  This should never happen
             |  Are there any other Session Managers connected to same MQ running?
             |  Sending poison pill to the executor.
           """.stripMargin)
        sessionExecutorClients.sendPoisonPill(workflowId)
    }
  }

  private def handleGet(id: Id): Option[Session] =
    sessionStateByWorkflowId.get(id).map(_.sessionForApi())

  private def handleList(): List[Session] = {
    sessionStateByWorkflowId.values.map(_.sessionForApi())
  }.toList

  private def handleKill(workflowId: Id): Unit = {
    sessionStateByWorkflowId.get(workflowId) match {
      case Some(session) =>
        session.kill()
        sessionStateByWorkflowId.remove(workflowId)
      case None =>
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
  case class LaunchRequest(id: Id) extends Request

}
