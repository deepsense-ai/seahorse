/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager.service.actors

import scala.collection.mutable
import scala.concurrent.ExecutionContext
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}

import akka.actor.{Actor, ActorRef}
import akka.pattern.pipe
import com.google.inject.Inject

import io.deepsense.commons.models.ClusterDetails
import io.deepsense.commons.utils.Logging
import io.deepsense.deeplang.Model.Id
import io.deepsense.sessionmanager.service.Session
import io.deepsense.sessionmanager.service.actors.SessionServiceActor._
import io.deepsense.sessionmanager.service.executor.SessionExecutorClients
import io.deepsense.sessionmanager.service.sessionspawner.{ExecutorSession, SessionConfig, SessionSpawner}
import io.deepsense.workflowexecutor.communication.message.global.Heartbeat
import io.deepsense.workflowexecutor.communication.mq.MQCommunication.Topic
import io.deepsense.workflowexecutor.rabbitmq.{ChannelSetupResult, MQCommunicationFactory}

class SessionServiceActor @Inject()(
  private val sessionSpawner: SessionSpawner,
  private val sessionExecutorClients: SessionExecutorClients,
  private val communicationFactory: MQCommunicationFactory
) extends Actor with Logging {

  implicit val ec: ExecutionContext = context.system.dispatcher

  // NOTE! No synchronising is needed because all mutations are in `receive` method call scope.
  // When modified from outside `receive` (possible through futures!) scope add synchronization
  val sessionStateByWorkflowId = mutable.Map.empty[Id, ExecutorSession]
  val workflowExecutionReportSubscribers = mutable.Map.empty[Id, (ActorRef, String, ActorRef)]

  override def receive: Receive = {
    case r: Request => handleRequest(r)
    case h: Heartbeat => handleHeartbeat(h)
    case (i: Id, subscriber: ActorRef, queue: String, chAct: ActorRef) =>
      workflowExecutionReportSubscribers(i) = (subscriber, queue, chAct)
    case x => unhandled(x)
  }

  private def handleRequest(request: Request): Unit = {
    request match {
      case GetRequest(id) => sender() ! handleGet(id)
      case KillRequest(id) => sender() ! handleKill(id)
      case ListRequest() => sender() ! handleList()
      case LaunchRequest(id) => sender() ! handleLaunch(id)
      case NodeStatusesRequest(id) => handleNodeStatusRequest(id, sender())
      case CreateRequest(sessionConfig, clusterConfig) =>
        sender() ! handleCreate(sessionConfig, clusterConfig)
    }
  }

  private def handleLaunch(id: Id): Try[Unit] = {
    if (sessionStateByWorkflowId.contains(id)) {
      Success(sessionExecutorClients.launchWorkflow(id))
    } else {
      Failure(sessionNotFoundException(id))
    }
  }

  private def sessionNotFoundException(id: Id) =
    new IllegalArgumentException(s"Session for the given id not found: $id")

  private def createSubscriberForWorkflowEvents(id: Id): ActorRef = {
    context.actorOf(ExecutionReportSubscriberActor(id))
  }

  private def subsribeForWorkflowEvents(id: Id): Unit = {
    val subscriber = createSubscriberForWorkflowEvents(id)
    val subscription =
      communicationFactory.registerSubscriber(Topic.workflowPublicationTopic(id, id.toString), subscriber)
    subscription.map({
      case ChannelSetupResult(queue, chAct) => (id, subscriber, queue, chAct)
    }) pipeTo self
    subscription.onFailure {
      case t => logger.error(s"Unable to subscribe SessionService to workflow $id execution events topic, " +
        s"this means the service can't provide node statuses reports.")
    }
  }

  private def handleCreate(
      sessionConfig: SessionConfig,
      clusterDetails: ClusterDetails): Session = {
    val workflowId = sessionConfig.workflowId
    subsribeForWorkflowEvents(workflowId)
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

  private def handleGet(id: Id): Option[ExecutorSession] = sessionStateByWorkflowId.get(id)

  private def handleList(): List[ExecutorSession] = sessionStateByWorkflowId.values.toList

  private def handleNodeStatusRequest(id: Id, whoAsks: ActorRef) = {
    if (!sessionStateByWorkflowId.contains(id)) {
      whoAsks ! Failure(sessionNotFoundException(id))
    } else {
      workflowExecutionReportSubscribers.get(id) match {
        case Some((actor, _, _)) => actor ! ExecutionReportSubscriberActor.ReportQuery(whoAsks)
        case None => whoAsks ! Failure(new NoSuchElementException(s"Subscriber for session id not found: $id"))
      }
    }
  }

  private def handleKill(workflowId: Id): Unit = {
    sessionStateByWorkflowId.get(workflowId) match {
      case Some(session) =>
        session.kill()
        sessionStateByWorkflowId.remove(workflowId)
        workflowExecutionReportSubscribers.get(workflowId).foreach({
          case (subscriber, queue, chAct) =>
            context.stop(subscriber)
            communicationFactory.deleteQueue(queue, chAct)
        })
        workflowExecutionReportSubscribers.remove(workflowId)
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
  case class NodeStatusesRequest(id: Id) extends Request

}
