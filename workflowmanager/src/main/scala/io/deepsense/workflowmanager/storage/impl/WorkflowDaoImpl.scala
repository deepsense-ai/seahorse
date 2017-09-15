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
import io.deepsense.workflowmanager.storage.{WorkflowStorage, WorkflowFullInfo}

case class WorkflowDaoImpl @Inject()(
    @Named("workflowmanager") db: JdbcDriver#API#Database,
    @Named("workflowmanager") driver: JdbcDriver,
    override val graphReader: GraphReader,
    @Named("scheduling-manager.user.id") schedulerUserId: String)
    (implicit ec: ExecutionContext)
  extends WorkflowStorage
  with WorkflowVersionUtil
  with Logging {

  import driver.api._

  override def get(id: Id): Future[Option[WorkflowFullInfo]] = {
    db.run(workflows.filter(w => w.id === id.value && w.deleted === false).result).map {
      case Seq() => None
      case Seq((_, workflow, _, created, updated, ownerId, ownerName)) =>
        Some(WorkflowFullInfo(
          workflow.parseJson.convertTo[Workflow],
          DateTimeConverter.fromMillis(created), DateTimeConverter.fromMillis(updated),
          ownerId, ownerName
        ))
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

  override def create(
      id: Id,
      workflow: Workflow,
      ownerId: String,
      ownerName: String): Future[Unit] = {
    db.run(workflows += ((id.value, workflow.toJson.compactPrint, false,
      DateTimeConverter.now.getMillis, DateTimeConverter.now.getMillis,
      ownerId, ownerName))).map(_ => ())
  }

  override def getAll(): Future[Map[Id, WorkflowFullInfo]] = {
    db.run(workflows.filterNot(w => w.deleted || w.ownerId === schedulerUserId).result).map(_.map {
      case (id, workflow, _, created, updated, ownerId, ownerName) =>
        Id.fromUuid(id) -> WorkflowFullInfo(
          workflow.parseJson.convertTo[Workflow],
          DateTimeConverter.fromMillis(created), DateTimeConverter.fromMillis(updated),
          ownerId, ownerName
        )
    }.toMap)
  }

  override def currentVersion: Version = CurrentBuild.version

  val WorkflowId = "id"
  val Workflow = "workflow"
  val Deleted = "deleted"
  val Created = "created"
  val Updated = "updated"
  val OwnerId = "owner_id"
  val OwnerName = "owner_name"

  private class Workflows(tag: Tag)
      extends Table[(UUID, String, Boolean, Long, Long, String, String)](tag, "WORKFLOWS") {

    def id: Rep[UUID] = column[UUID](WorkflowId, O.PrimaryKey)
    def workflow: Rep[String] = column[String](Workflow)
    def deleted: Rep[Boolean] = column[Boolean](Deleted)
    def created: Rep[Long] = column[Long](Created)
    def updated: Rep[Long] = column[Long](Updated)
    def ownerId: Rep[String] = column[String](OwnerId)
    def ownerName: Rep[String] = column[String](OwnerName)

    def * : ProvenShape[(UUID, String, Boolean, Long, Long, String, String)] =
      (id, workflow, deleted, created, updated, ownerId, ownerName)
  }

  private val workflows = TableQuery[Workflows]

  private[impl] def create(): Future[Unit] = db.run(workflows.schema.create)
  private[impl] def drop(): Future[Unit] = db.run(workflows.schema.drop)
}
