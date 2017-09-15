/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.workflowmanager.storage.impl

import java.util.UUID

import scala.concurrent.{ExecutionContext, Future}

import com.google.inject.Inject
import com.google.inject.name.Named
import slick.driver.JdbcDriver
import slick.lifted.{Index, PrimaryKey, ProvenShape}

import io.deepsense.graph.Node
import io.deepsense.models.workflows.Workflow.Id
import io.deepsense.workflowmanager.storage.NotebookStorage

class NotebookDaoImpl @Inject()(
    @Named("workflowmanager") db: JdbcDriver#API#Database,
    @Named("workflowmanager") driver: JdbcDriver)
    (implicit ec: ExecutionContext)
  extends NotebookStorage {

  import driver.api._

  override def get(workflowId: Id, nodeId: Node.Id): Future[Option[String]] = {
    db.run(
      notebooks.filter(n => n.workflowId === workflowId.value && n.nodeId === nodeId.value).result
    ).map {
      case Seq() => None
      case Seq((_, _, notebook)) => Some(notebook)
    }
  }

  override def save(workflowId: Id, nodeId: Node.Id, notebook: String): Future[Unit] =
    db.run(notebooks.insertOrUpdate((workflowId.value, nodeId.value, notebook))).map(_ => ())

  override def getAll(workflowId: Id): Future[Map[Node.Id, String]] = {
    db.run(notebooks.filter(_.workflowId === workflowId.value).result).map(
      _.map {
        case (_, nodeId, notebook) => Node.Id.fromUuid(nodeId) -> notebook
      }.toMap)
  }

  val WorkflowId = "workflow_id"
  val NodeId = "node_id"
  val Notebook = "notebook"

  private class Notebooks(tag: Tag)
    extends Table[(UUID, UUID, String)](tag, "NOTEBOOKS") {

    def workflowId: Rep[UUID] = column[UUID](WorkflowId)
    def nodeId: Rep[UUID] = column[UUID](NodeId)
    def notebook: Rep[String] = column[String](Notebook)

    def pk: PrimaryKey = primaryKey("pk_notebooks", (workflowId, nodeId))
    def index: Index = index("idx_notebooks_workflow_id", workflowId, unique = false)

    def * : ProvenShape[(UUID, UUID, String)] =
      (workflowId, nodeId, notebook)
  }

  private val notebooks = TableQuery[Notebooks]

  private[impl] def create(): Future[Unit] = db.run(notebooks.schema.create)
  private[impl] def drop(): Future[Unit] = db.run(notebooks.schema.drop)
}
