/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager.service.eventstore

import java.sql.{SQLException, Timestamp}
import java.util.UUID

import scala.concurrent.{ExecutionContext, Future}

import com.google.inject.Inject
import com.google.inject.name.Named
import org.joda.time.{DateTime, DateTimeZone}
import slick.driver.JdbcDriver
import slick.lifted.ProvenShape

import io.deepsense.commons.datetime.DateTimeConverter
import io.deepsense.commons.models.Id
import io.deepsense.sessionmanager.service.EventStore
import io.deepsense.sessionmanager.service.EventStore._

class PersistentEventStore @Inject() (
  @Named("EventStore") db: JdbcDriver#API#Database,
  @Named("EventStore") driver: JdbcDriver,
  @Named("eventstore.table") tableName: String
)(implicit ec: ExecutionContext) extends EventStore {

  import driver.api._

  private val startedName = "started"
  private val heartbeatName = "heartbeat"

  private implicit def timestampToDatatime(t: Timestamp): DateTime =
    new DateTime(t.getTime, DateTimeZone.UTC)

  private implicit def dateTimeToTimestamp(d: DateTime): Timestamp =
    new Timestamp(d.toDateTime(DateTimeZone.UTC).getMillis)

  private def rowToEvent(row: (UUID, String, Timestamp)): Event = {
    row match {
      case (id, name, at) if name == startedName =>
        Started(id, at)
      case (id, name, at) if name == heartbeatName =>
        HeartbeatReceived(id, at)
    }
  }

  override def getLastEvent(workflowId: Id): Future[Option[Event]] = {
    val query = sessions
      .filter(_.workflowId === workflowId.value)
      .result
      .headOption
      .map(_.map(rowToEvent))
    db.run(query)
  }

  override def getLastEvents: Future[Map[Id, Event]] = {
    val query = sessions.result
    db.run(query).map(_.map(rowToEvent).map(e => (e.workflowId, e)).toMap)
  }

  override def killed(workflowId: Id): Future[Unit] = {
    val query = sessions.filter(_.workflowId === workflowId.value).delete
    db.run(query).map(_ => Unit)
  }

  override def heartbeat(workflowId: Id): Future[Either[InvalidWorkflowId, Unit]] = {
    val query = sessions.filter(_.workflowId === workflowId.value)
        .map(event => (event.event, event.at))

    val update = query.update(heartbeatName, DateTimeConverter.now)
    db.run(update).map {
      case 1 => Right(())
      case _ => Left(InvalidWorkflowId())
    }
  }

  override def started(workflowId: Id): Future[Either[SessionExists, Unit]] = {
    val insert = sessions += (workflowId.value, startedName, DateTimeConverter.now)
    db.run(insert).map {
      case 1 => Right(())
    }.recover {
      case ex: SQLException if matchesError(ex, ErrorCodes.UniqueViolation) =>
        Left(SessionExists())
    }
  }

  private type RowType = (UUID, String, Timestamp)

  private class SessionsTable(tag: Tag)
    extends Table[RowType](tag, tableName) {

    private val workflowIdColumn = "workflow"
    private val eventColumn = "event"
    private val atColumn = "at"

    def workflowId: Rep[UUID] = column[UUID](workflowIdColumn, O.PrimaryKey)
    def event: Rep[String] = column[String](eventColumn)
    def at: Rep[Timestamp] = column[Timestamp](atColumn)

    override def * : ProvenShape[RowType] = (workflowId, event, at)
  }

  private val sessions = TableQuery[SessionsTable]

  private[eventstore] def create(): Future[Unit] = {
    db.run(sessions.schema.create)
  }

  object ErrorCodes {
    val UniqueViolation = 23505 // Defined in SQL Standard
  }

  def matchesError(ex: java.sql.SQLException, errorCode: Int): Boolean =
    ex.getErrorCode == errorCode
}
