/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.workflowmanager.storage.impl

import java.util.UUID

import scala.concurrent.{ExecutionContext, Future}

import com.google.inject.Inject
import com.google.inject.name.Named
import slick.driver.JdbcDriver
import slick.lifted.ProvenShape
import spray.json._

import io.deepsense.commons.datetime.DateTimeConverter
import io.deepsense.commons.utils.{Logging, Version}
import io.deepsense.models.json.graph.GraphJsonProtocol.GraphReader
import io.deepsense.models.json.workflow.WorkflowVersionUtil
import io.deepsense.models.workflows.Workflow
import io.deepsense.models.workflows.Workflow.Id
import io.deepsense.workflowmanager.rest.CurrentBuild
import io.deepsense.workflowmanager.storage.{WorkflowStorage, WorkflowWithDates}

case class WorkflowDaoImpl @Inject()(
    @Named("workflowmanager") db: JdbcDriver#API#Database,
    @Named("workflowmanager") driver: JdbcDriver,
    override val graphReader: GraphReader)
    (implicit ec: ExecutionContext)
  extends WorkflowStorage
  with WorkflowVersionUtil
  with Logging {

  import driver.api._

  override def get(id: Id): Future[Option[Workflow]] = {
    db.run(workflows.filter(w => w.id === id.value && w.deleted === false).result).map {
      case Seq() => None
      case Seq((_, workflow, _, _, _)) => Some(workflow.parseJson.convertTo[Workflow])
    }
  }

  override def update(id: Id, workflow: Workflow): Future[Unit] = {
    val query = for { w <- workflows if w.id === id.value } yield (w.workflow, w.updated)
    db.run(query.update((workflow.toJson.compactPrint, DateTimeConverter.now.getMillis)))
      .map(_ => ())
  }

  override def delete(id: Id): Future[Unit] = {
    val query = for { w <- workflows if w.id === id.value } yield w.deleted
    db.run(query.update(true)).map(_ => ())
  }

  override def create(id: Id, workflow: Workflow): Future[Unit] = {
    db.run(workflows += ((id.value, workflow.toJson.compactPrint, false,
      DateTimeConverter.now.getMillis, DateTimeConverter.now.getMillis))).map(_ => ())
  }

  override def getAll(): Future[Map[Id, WorkflowWithDates]] = {
    db.run(workflows.filter(_.deleted === false).result).map(_.map {
      case (id, workflow, _, created, updated) => Id.fromUuid(id) -> WorkflowWithDates(
        workflow.parseJson.convertTo[Workflow],
        DateTimeConverter.fromMillis(created), DateTimeConverter.fromMillis(updated)
      )
    }.toMap)
  }

  override def currentVersion: Version = CurrentBuild.version

  val WorkflowId = "id"
  val Workflow = "workflow"
  val Deleted = "deleted"
  val Created = "created"
  val Updated = "updated"

  private class Workflows(tag: Tag)
      extends Table[(UUID, String, Boolean, Long, Long)](tag, "WORKFLOWS") {

    def id: Rep[UUID] = column[UUID](WorkflowId, O.PrimaryKey)
    def workflow: Rep[String] = column[String](Workflow)
    def deleted: Rep[Boolean] = column[Boolean](Deleted)
    def created: Rep[Long] = column[Long](Created)
    def updated: Rep[Long] = column[Long](Updated)

    def * : ProvenShape[(UUID, String, Boolean, Long, Long)] =
      (id, workflow, deleted, created, updated)
  }

  private val workflows = TableQuery[Workflows]

  private[impl] def create(): Future[Unit] = db.run(workflows.schema.create)
  private[impl] def drop(): Future[Unit] = db.run(workflows.schema.drop)
}
