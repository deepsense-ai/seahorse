/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager.storage.inmemory

import scala.collection.concurrent.TrieMap
import scala.concurrent.Future
import scala.util.Random

import io.deepsense.commons.models.Id
import io.deepsense.sessionmanager.storage.SessionStorage._
import io.deepsense.sessionmanager.storage._

/**
 * Thread-safe, in-memory SessionStorage.
 */
class InMemorySessionStorage(initRows: Seq[SessionRow] = Seq.empty) extends SessionStorage {
  private val sessions: TrieMap[Id, SessionRow] = TrieMap()

  initRows.foreach(store)

  override def create(id: Id): Future[CreateResult] = {
    val version = Random.nextInt()
    val row = SessionRow(id, None, version)
    val optEntry = sessions.putIfAbsent(id, row)
    Future.successful(optEntry match {
      case None => Right(CreateSucceeded(version))
      case Some(_) => Left(CreateFailed())
    })
  }

  override def setBatchId(id: Id, batchId: Int, lastVersion: Int): Future[SetBatchIdResult] = {
    val optSessionRow = sessions.get(id)
    val result = optSessionRow match {
      case None => Left(OptimisticLockFailed())
      case Some(oldSession@SessionRow(_, None, `lastVersion`)) =>
        val newVersion = Random.nextInt()
        val newSessionRow = oldSession.copy(optBatchId = Some(batchId), version = newVersion)
        sessions.replace(id, oldSession, newSessionRow) match {
          case true => Right(SetBatchIdSucceeded(newVersion))
          case false => Left(OptimisticLockFailed())
        }
      case Some(SessionRow(_, _, _)) =>
        Left(OptimisticLockFailed())
    }
    Future.successful(result)
  }

  override def get(id: Id): Future[Option[SessionRow]] = {
    Future.successful(sessions.get(id))
  }

  override def getAll: Future[Map[Id, SessionRow]] = {
    Future.successful(sessions.toMap)
  }

  override def delete(id: Id, lastVersion: Int): Future[DeleteResult] = {
    val optSessionRow = sessions.get(id)
    val result = optSessionRow match {
      case None => Left(OptimisticLockFailed())
      case Some(session@SessionRow(_, optBatchId, `lastVersion`)) =>
        sessions.remove(id, session) match {
          case true => Right(DeleteSucceeded())
          case false => Left(OptimisticLockFailed())
        }
      case Some(session@SessionRow(_, _, _)) =>
        Left(OptimisticLockFailed())
    }
    Future.successful(result)
  }

  private def store(row: SessionRow): Future[Unit] = {
    Future.successful(sessions.putIfAbsent(row.workflowId, row))
  }
}
