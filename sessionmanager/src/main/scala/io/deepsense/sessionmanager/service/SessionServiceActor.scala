/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager.service

import scala.collection.mutable
import scala.concurrent.Future

import akka.actor.Actor
import akka.pattern.pipe

import io.deepsense.commons.models.Id
import io.deepsense.commons.utils.Logging
import io.deepsense.sessionmanager.service.SessionServiceActor._
import io.deepsense.sessionmanager.service.livy.Livy
import io.deepsense.sessionmanager.service.livy.responses.Batch

class SessionServiceActor private[service](
  private val livyClient: Livy,
  private val initialHandles: Seq[LivySessionHandle] = Seq.empty,
  private val initialFutureSessions: Map[Id, Future[Session]] = Map.empty
) extends Actor with Logging {

  private val sessions =
    mutable.Map[Id, LivySessionHandle](initialHandles.map(h => (h.workflowId, h)): _*)
  private val creating =
    mutable.Map[Id, Future[Session]](initialFutureSessions.toSeq: _*)

  import scala.concurrent.ExecutionContext.Implicits.global

  override def receive: Receive = {
    case r: Request => handleRequest(r)
    case sm: SelfMessage => handleSelfMessage(sm)
    case x => unhandled(x)
  }

  private def handleRequest(request: Request): Unit = {
    request match {
      case GetRequest(id) =>
        val session = creating.get(id).orElse(sessions.get(id).map(sessionFromHandle))
        transform(session) pipeTo sender()
      case KillRequest(id) => handleKill(id)
      case ListRequest() => handleList()
      case CreateRequest(id) => handleCreate(id)
    }
  }

  private def handleSelfMessage(x: SelfMessage): Unit = x match {
    case SelfLivySessionHandle(handle) =>
      creating.remove(handle.workflowId)
      sessions.put(handle.workflowId, handle)
    case SelfKillResponse(response) =>
      sessions.remove(response.id)
  }

  private def handleKill(id: Id): Unit = {
    if (sessions.contains(id)) {
      sessions(id) match {
        case LivySessionHandle(workflowId, batchId) =>
          val message = livyClient.killSession(batchId).map {
            k => KillResponse(id, k)
          }
          pipeResponseToSelf(message)
          message pipeTo sender()
      }
    } else {
      sender() ! KillResponse(id, killed = false)
    }
  }

  private def handleList(): Unit = {
    val batches = livyClient.listSessions()
    val sessionsToSend = batches.map {
      batchesList =>
        val livyBatches = batchesList.sessions.map(b => (b.id, b)).toMap
        val rawSessions = sessions.map {
          case (id, handle) =>
            livyBatches
              .get(handle.batchId)
              .map(b => createSession(handle, b))
        }
        val flattenSessions = rawSessions.flatten
        flattenSessions.toList
    }
    sessionsToSend pipeTo sender()
  }

  private def sessionFromHandle(handle: LivySessionHandle): Future[Session] = {
    handle match {
      case LivySessionHandle(workflowId, batchId) =>
        val livyBatch = livyClient.getSession(batchId)
        livyBatch.collect {
          case None =>
            logger.error(s"Livy's Batch($batchId) for Workflow '$workflowId' does not exist!")
        }
        livyBatch.map(_.get).map {
          batch =>
            createSession(handle, batch)
        }
    }
  }

  private def createSession(handle: LivySessionHandle, batch: Batch): Session = {
    Session(handle, Status.fromBatchStatus(batch.status))
  }

  private def handleCreate(workflowId: Id): Unit = {
    creating.get(workflowId) match {
      case Some(futureSession) => futureSession pipeTo sender()
      case None =>
        sessions.get(workflowId) match {
          case Some(handle) => sessionFromHandle(handle) pipeTo sender()
          case None =>
            val livyBatch = livyClient.createSession(workflowId)
            val livyHandle = livyBatch.map {
              batch =>
                LivySessionHandle(workflowId, batch.id)
            }

            pipeHandleToSelf(livyHandle)

            val session = livyBatch.flatMap { batch =>
                livyHandle.map {
                  h => createSession(h, batch)
                }
            }
            creating.put(workflowId, session)
            session pipeTo sender()
        }
    }
  }

  private def transform[T](o: Option[Future[T]]): Future[Option[T]] =
    o.map(f => f.map(Option(_))).getOrElse(Future.successful(None))

  private sealed trait SelfMessage
  private case class SelfLivySessionHandle(handle: LivySessionHandle) extends SelfMessage
  private case class SelfKillResponse(response: KillResponse) extends SelfMessage

  private def pipeResponseToSelf(msg: Future[KillResponse]): Unit =
    msg.map(SelfKillResponse) pipeTo self

  private def pipeHandleToSelf(msg: Future[LivySessionHandle]): Unit =
    msg.map(SelfLivySessionHandle) pipeTo self
}

object SessionServiceActor {
  sealed trait Request
  case class GetRequest(id: Id) extends Request
  case class KillRequest(id: Id) extends Request
  case class ListRequest() extends Request
  case class CreateRequest(workflowId: Id) extends Request

  sealed trait Response
  case class KillResponse(id: Id, killed: Boolean) extends Response
}
