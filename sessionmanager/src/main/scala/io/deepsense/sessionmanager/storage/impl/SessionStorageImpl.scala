/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager.storage.impl

import java.sql.SQLException
import java.util.UUID

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

import com.google.inject.Inject
import com.google.inject.name.Named
import slick.driver.JdbcDriver
import slick.lifted.{Index, ProvenShape}

import io.deepsense.commons.models.Id
import io.deepsense.commons.utils.Logging
import io.deepsense.sessionmanager.storage.SessionStorage._
import io.deepsense.sessionmanager.storage._

case class SessionStorageImpl (
    db: JdbcDriver#API#Database,
    driver: JdbcDriver,
    tableName: String,
    initRows: Seq[SessionRow])
    (implicit ec: ExecutionContext)
  extends SessionStorage
  with Logging {

  @Inject()
  def this(
      @Named("SessionManager") db: JdbcDriver#API#Database,
      @Named("SessionManager") driver: JdbcDriver,
      @Named("session-table-name") tableName: String)(implicit ec: ExecutionContext) = {
    this(db, driver, tableName, Seq.empty)
  }

  import driver.api._

  initRows.foreach(store)

  override def create(workflowId: Id): Future[CreateResult] = {
    val version = Random.nextInt()
    val query = sessions += ((workflowId.value, None, version))
    db.run(query).map(a => Right(CreateSucceeded(version))).recover {
      case ex: SQLException if matchesError(ex, ErrorCode.UniqueViolation) =>
        Left(CreateFailed())
    }
  }

  override def setBatchId(id: Id, batchId: Int, lastVersion: Int): Future[SetBatchIdResult] = {
    val newVersion = Random.nextInt()
    val purge = sessions.filter(s => s.batchId === batchId).delete
    val query =
      (for {
        s <- sessions if s.id === id.value && s.version === lastVersion
      } yield (s.batchId, s.version)).update(Some(batchId), newVersion)
    db.run(purge).flatMap(d => {
      if (d != 0) logger.error(s"Purged non existing livy session $batchId")
      db.run(query).map {
        case 1 => Right(SetBatchIdSucceeded(newVersion))
        case 0 => Left(OptimisticLockFailed())
      }
    })
  }

  override def get(id: Id): Future[Option[SessionRow]] = {
    db.run(sessions.filter(s => s.id === id.value).result).map {
      case Seq() => None
      case Seq((_, batchId, version)) => Some(SessionRow(id, batchId, version))
    }
  }

  override def delete(id: Id, version: Int): Future[DeleteResult] = {
    val query = for { s <- sessions if s.id === id.value && s.version === version } yield s
    db.run(query.delete).map {
      case 1 => Right(DeleteSucceeded())
      case 0 => Left(OptimisticLockFailed())
    }
  }

  override def getAll: Future[Map[Id, SessionRow]] = {
    db.run(sessions.result).map(_.map {
        case (id, batchId, version) => Id.fromUuid(id) -> SessionRow(id, batchId, version)
      }.toMap
    )
  }

  private [impl] def store(sessionRow: SessionRow): Future[Unit] = {
    val query =
      sessions += ((sessionRow.workflowId.value, sessionRow.optBatchId, sessionRow.version))
    db.run(query).map(_ => ())
  }

  val WorkflowId = "id"
  val BatchId = "batch_id"
  val Version = "version"

  private class Sessions(tag: Tag)
    extends Table[(UUID, Option[Int], Int)](tag, tableName) {

    def id: Rep[UUID] = column[UUID](WorkflowId, O.PrimaryKey)
    def batchId: Rep[Option[Int]] = column[Option[Int]](BatchId)
    def version: Rep[Int] = column[Int](Version)

    def * : ProvenShape[(UUID, Option[Int], Int)] =
      (id, batchId, version)

    def idxBatchId: Index = index(BatchId, batchId, unique = true)
  }

  private val sessions = TableQuery[Sessions]

  private[impl] def create(): Future[Unit] = db.run(sessions.schema.create)
  private[impl] def drop(): Future[Unit] = db.run(sessions.schema.drop)
}
