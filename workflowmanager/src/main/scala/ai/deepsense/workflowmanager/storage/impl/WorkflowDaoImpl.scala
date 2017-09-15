/**
 * Copyright 2016 deepsense.ai (CodiLime, Inc)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ai.deepsense.workflowmanager.storage.impl

import java.util.UUID

import scala.concurrent.{ExecutionContext, Future}

import com.google.inject.Inject
import com.google.inject.name.Named
import slick.driver.JdbcDriver
import slick.lifted.ProvenShape
import spray.json._

import ai.deepsense.commons.datetime.DateTimeConverter
import ai.deepsense.commons.utils.{Logging, Version}
import ai.deepsense.models.json.graph.GraphJsonProtocol.GraphReader
import ai.deepsense.models.json.workflow.WorkflowVersionUtil
import ai.deepsense.models.workflows.Workflow
import ai.deepsense.models.workflows.Workflow.Id
import ai.deepsense.workflowmanager.rest.CurrentBuild
import ai.deepsense.workflowmanager.storage.{WorkflowFullInfo, WorkflowStorage, WorkflowRaw}

case class WorkflowDaoImpl @Inject()(
    @Named("workflowmanager") db: JdbcDriver#API#Database,
    @Named("workflowmanager") driver: JdbcDriver,
    override val graphReader: GraphReader,
    @Named("predefined-users.scheduler.id") schedulerUserId: String)
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

  override def updateRaw(id: Id, workflow: JsValue): Future[Unit] = {
    val query = for { w <- workflows if w.id === id.value } yield (w.workflow, w.updated)
    db.run(query.update((workflow.compactPrint, DateTimeConverter.now.getMillis)))
      .map(_ => ())
  }

  override def delete(id: Id): Future[Unit] = {
    val query = for { w <- workflows if w.id === id.value } yield w.deleted
    db.run(query.update(true)).map(_ => ())
  }

  override def createRaw(
      id: Id,
      workflow: JsValue,
      ownerId: String,
      ownerName: String): Future[Unit] = {
    db.run(workflows += ((id.value, workflow.compactPrint, false,
      DateTimeConverter.now.getMillis, DateTimeConverter.now.getMillis,
      ownerId, ownerName))).map(_ => ())
  }


  override def getAllRaw: Future[Map[Id, WorkflowRaw]] = {
    db.run(workflows.filterNot(w => w.deleted || w.ownerId === schedulerUserId).result).map(_.map {
      case (id, workflow, _, created, updated, ownerId, ownerName) =>
        Id.fromUuid(id) -> WorkflowRaw(
          workflow.parseJson,
          DateTimeConverter.fromMillis(created),
          DateTimeConverter.fromMillis(updated),
          ownerId, ownerName
        )
    }.toMap)
  }

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
