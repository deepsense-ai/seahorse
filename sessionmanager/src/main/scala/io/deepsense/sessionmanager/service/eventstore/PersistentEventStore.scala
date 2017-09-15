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
import io.deepsense.commons.utils.Logging
import io.deepsense.sessionmanager.rest.requests.ClusterDetails
import io.deepsense.sessionmanager.service.EventStore
import io.deepsense.sessionmanager.service.EventStore._
import io.deepsense.sessionmanager.rest.ClusterDetailsJsonProtocol._
import spray.json._

class PersistentEventStore @Inject() (
  @Named("EventStore") db: JdbcDriver#API#Database,
  @Named("EventStore") driver: JdbcDriver,
  @Named("eventstore.table") tableName: String
)(implicit ec: ExecutionContext) extends EventStore with Logging {

  import driver.api._

  private val startedName = "started"
  private val heartbeatName = "heartbeat"

  private implicit def timestampToDatatime(t: Timestamp): DateTime =
    new DateTime(t.getTime, DateTimeZone.UTC)

  private implicit def dateTimeToTimestamp(d: DateTime): Timestamp =
    new Timestamp(d.toDateTime(DateTimeZone.UTC).getMillis)

  private def deserializeClusterDetails(serializedConfig: String): ClusterDetails = {
    val jsonAst = serializedConfig.parseJson
    jsonAst.convertTo[ClusterDetails]
  }

  private def rowToEvent(row: (UUID, String, Timestamp, String)): Event = {
    row match {
      case (id, name, at, cluster) if name == startedName =>
        Started(id, at, deserializeClusterDetails(cluster))
      case (id, name, at, cluster) if name == heartbeatName =>
        HeartbeatReceived(id, at, deserializeClusterDetails(cluster))
    }
  }

  override def getLastEvent(workflowId: Id): Future[Option[Event]] = {
    val query = sessions
      .filter(_.workflowId === workflowId.value)
      .result
      .headOption
      .map(_.map(rowToEvent))
    val event = db.run(query)
    event.onSuccess {
      case x => logger.info(s"LastEvent for '$workflowId' is: $x")
    }

    event
  }

  override def getLastEvents: Future[Map[Id, Event]] = {
    val query = sessions.result
    val lastEvents = db.run(query).map(_.map(rowToEvent)
        .map(e => (e.workflowId, e)).toMap)
    lastEvents.onSuccess {
      case x => logger.info(s"LastEvents are: $x")
    }
    lastEvents
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
      case 1 =>
        logger.trace(s"Got Heartbeat: $workflowId")
        Right(())
      case _ =>
        logger.debug(s"Got incorrect Heartbeat: $workflowId")
        Left(InvalidWorkflowId())
    }
  }

  override def started(workflowId: Id, clusterDetails: ClusterDetails):
      Future[Either[SessionExists, Unit]] = {
    val serializedCluster = clusterDetails.toJson.toString
    val insert = sessions +=
        (workflowId.value, startedName, DateTimeConverter.now, serializedCluster)
    db.run(insert).map {
      case 1 =>
        logger.debug(s"Recored a new session $workflowId")
        Right(())
    }.recover {
      case ex: SQLException if matchesError(ex, ErrorCodes.UniqueViolation) =>
        logger.debug(s"Session $workflowId already exists!")
        Left(SessionExists())
    }
  }

  private type RowType = (UUID, String, Timestamp, String)

  private class SessionsTable(tag: Tag)
    extends Table[RowType](tag, tableName) {

    private val workflowIdColumn = "workflow"
    private val eventColumn = "event"
    private val atColumn = "at"
    private val clusterConfigColumn = "selializedClusterConfig"

    def workflowId: Rep[UUID] = column[UUID](workflowIdColumn, O.PrimaryKey)
    def event: Rep[String] = column[String](eventColumn)
    def at: Rep[Timestamp] = column[Timestamp](atColumn)
    def clusterConfig: Rep[String] = column[String](clusterConfigColumn)


    override def * : ProvenShape[RowType] = (workflowId, event, at, clusterConfig)
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
