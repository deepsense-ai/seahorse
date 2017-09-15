/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager.storage

import scala.concurrent.Future
import scala.util.Random

import io.deepsense.commons.models.Id
import io.deepsense.sessionmanager.service.LivySessionHandle
import io.deepsense.sessionmanager.storage.SessionStorage._

trait SessionStorage {

  def get(id: Id): Future[Option[SessionRow]]

  def getAll: Future[Map[Id, SessionRow]]

  /**
    * Creates a session without batchId yet.
    */
  def create(id: Id): Future[CreateResult]

  /**
    * Sets batchId to a session. If the session version changed returns an OptimisticLockFailed.
    */
  def setBatchId(id: Id, batchId: Int, lastVersion: Int): Future[SetBatchIdResult]

  /**
    * Removes a session (with or without batchId).
    * If the session version changed returns an OptimisticLockFailed.
    */
  def delete(id: Id, lastVersion: Int): Future[DeleteResult]
}

object SessionStorage {
  type CreateResult = Either[CreateFailed, CreateSucceeded]
  type SetBatchIdResult = Either[OptimisticLockFailed, SetBatchIdSucceeded]
  type DeleteResult = Either[OptimisticLockFailed, DeleteSucceeded]

  case class SessionRow(workflowId: Id, optBatchId: Option[Int], version: Int = Random.nextInt()) {
    def handle: Option[LivySessionHandle] = optBatchId.map {
      batchId =>
        LivySessionHandle(workflowId, batchId)
    }
  }

  case class OptimisticLockFailed()
  case class CreateFailed()
  case class CreateSucceeded(version: Int)
  case class SetBatchIdSucceeded(newVersion: Int)
  case class DeleteSucceeded()

  object ErrorCode {
    val UniqueViolation = 23505
  }

  def matchesError(ex: java.sql.SQLException, errorCode: Int): Boolean = {
    ex.getErrorCode == errorCode
  }
}
