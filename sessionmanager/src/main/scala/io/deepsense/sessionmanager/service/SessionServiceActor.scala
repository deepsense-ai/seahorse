/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager.service

import scala.concurrent.Future

import akka.actor.Actor
import akka.pattern.pipe
import com.google.inject.Inject

import io.deepsense.commons.models.Id
import io.deepsense.commons.utils.Logging
import io.deepsense.sessionmanager.service.SessionServiceActor._
import io.deepsense.sessionmanager.service.livy.Livy
import io.deepsense.sessionmanager.service.livy.responses.Batch
import io.deepsense.sessionmanager.storage.SessionStorage._
import io.deepsense.sessionmanager.storage._

class SessionServiceActor @Inject()(
  private val livyClient: Livy,
  private val sessionStorage: SessionStorage
) extends Actor with Logging {

  import scala.concurrent.ExecutionContext.Implicits.global

  override def receive: Receive = {
    case r: Request => handleRequest(r)
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

  private def handleGet(id: Id): Future[Option[Session]] = {
    sessionStorage.get(id).flatMap(toSession)
  }

  private def handleList(): Future[Seq[Session]] = {
    val futureBatchesMap = livyClient.listSessions().map(_.sessions.map(b => (b.id, b)).toMap)
    val futureSessionsMap = sessionStorage.getAll

    val running = purgeNonExistentSessions(futureSessionsMap, futureBatchesMap)
    val creating = creatingSessions(futureSessionsMap)

    for {
      seqRunning <- running
      seqCreating <- creating
    } yield seqRunning ++ seqCreating
  }

  private def handleCreate(id: Id): Future[Id] = {
    sessionStorage.create(id).flatMap {
      case Right(CreateSucceeded(version)) =>
        val livyBatch = livyClient.createSession(id)
        livyBatch.onFailure {
          case ex => sessionStorage.delete(id, version)
        }
        livyBatch.flatMap {
          batch =>
            sessionStorage.setBatchId(id, batch.id, version).flatMap {
              case Right(SetBatchIdSucceeded(_)) =>
                Future.successful(id)
              case Left(OptimisticLockFailed()) =>
                livyClient.killSession(batch.id).flatMap(_ => handleCreate(id))
            }
        }
      case Left(CreateFailed()) => Future.successful(id)
    }
  }

  private def handleKill(id: Id): Future[Unit] = {
    sessionStorage.get(id).flatMap {
      case None =>
        Future.successful(())
      case Some(SessionRow(_, Some(batchId), version)) =>
        livyClient.killSession(batchId).flatMap {
          _ => sessionStorage.delete(id, version).map {
            _ => ()
          }
        }
      case Some(SessionRow(_, None, version)) =>
        logger.error(s"Kill request for session in creating state! No Livy session will be killed.")
        sessionStorage.delete(id, version).map {
          _ => ()
        }
    }
  }

  private def toSession(optSessionRow: Option[SessionRow]): Future[Option[Session]] = {
    optSessionRow match {
      case Some(sessionRow) =>
        val id = sessionRow.workflowId
        val version = sessionRow.version
        sessionRow.optBatchId match {
          case Some(batchId) =>
            val livyBatch = livyClient.getSession(batchId)
            livyBatch.flatMap {
              case None =>
                deleteNonExistentSession(id, batchId, version).map(_ => None)
              case Some(batch) =>
                Future.successful(Some(Session(id, batch)))
            }
          case None =>
            Future.successful(Some(Session(id)))
        }
      case None => Future.successful(None)
    }
  }

  private def creatingSessions(storedSessions: Future[Map[Id, SessionRow]])
      : Future[Seq[Session]] = {
    storedSessions.map {
      case sessionsMap =>
        (for ((id, sessionRow) <- sessionsMap if sessionRow.optBatchId.isEmpty)
          yield Session(id, None, Status.Creating)).toSeq
    }
  }

  private def purgeNonExistentSessions(
      storedSessions: Future[Map[Id, SessionRow]],
      livySessions: Future[Map[Int, Batch]] ): Future[Seq[Session]] = {
    for {
      sessionMap <- storedSessions
      batchesMap <- livySessions
      running <- Future.sequence(
        sessionMap.values.collect {
          case SessionRow(id, Some(batchId), version) if batchesMap.contains(batchId) =>
            Future.successful(Some(Session(id, batchesMap.get(batchId).get)))
          case SessionRow(id, Some(batchId), version) if !batchesMap.contains(batchId) =>
            deleteNonExistentSession(id, batchId, version).map(_ => None)
        }.toSeq)
    } yield running.flatten
  }

  private def deleteNonExistentSession(id: Id, batchId: Int, version: Int): Future[DeleteResult] = {
    logger.error(
      s"""Livy's Batch($batchId) for Workflow('$id') does not exist!
          |Removing from storage...""".stripMargin)
    sessionStorage.delete(id, version).map(
      result => {
        logger.error(s"Livy's Batch($batchId) and Workflow('$id') removed from storage")
        result
      })
  }
}

object SessionServiceActor {
  sealed trait Request
  case class GetRequest(id: Id) extends Request
  case class KillRequest(id: Id) extends Request
  case class ListRequest() extends Request
  case class CreateRequest(workflowId: Id) extends Request
}
