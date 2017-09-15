/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.workflowmanager.storage.cassandra

import scala.concurrent.{ExecutionContext, Future}

import com.datastax.driver.core.{Row, Session}
import com.datastax.driver.core.querybuilder.QueryBuilder._
import com.datastax.driver.core.querybuilder.{QueryBuilder, Select, Update}
import com.google.inject.Inject
import com.google.inject.name.Named

import io.deepsense.commons.utils.Logging
import io.deepsense.graph.Node
import io.deepsense.models.workflows.Workflow
import io.deepsense.workflowmanager.storage.NotebookStorage

class NotebookDaoCassandraImpl @Inject() (
    @Named("cassandra.workflowmanager.notebook.table") table: String,
    @Named("WorkflowsSession") session: Session)
    (implicit ec: ExecutionContext)
  extends NotebookStorage
  with Logging {

  import NotebookDaoCassandraImpl._

  private val queryBuilder = new QueryBuilder(session.getCluster)

  override def get(workflowId: Workflow.Id, nodeId: Node.Id): Future[Option[String]] = {
    Future(session.execute(getQuery(workflowId, nodeId))).map(rs => {
      val rows = rs.all()
      rows.size match {
        case 0 => None
        case 1 => Some(rows.get(0).getString(Notebook))
        case n =>
          logger.error(s"Query returned ${n} rows for notebook " +
            s"with workflow id ${workflowId.value} and node id ${nodeId.value}")
          throw new IllegalStateException(s"Multiple notebooks " +
            s"with workflow id ${workflowId.value} and node id ${nodeId.value}")
      }
    })
  }

  override def save(workflowId: Workflow.Id, nodeId: Node.Id, notebook: String): Future[Unit] = {
    Future(session.execute(updateQuery(workflowId, nodeId, notebook)))
  }

  override def getAll(workflowId: Workflow.Id): Future[Map[Node.Id, String]] = {
    Future(session.execute(getAllQuery(workflowId))).map(
      _.all().toArray(new Array[Row](0)).map {
        row => Node.Id(row.getUUID(NodeId)) -> row.getString(Notebook)
    }.toMap)
  }

  private def getQuery(
      workflowId: Workflow.Id,
      nodeId: Node.Id): Select.Where = {
    queryBuilder.select(Notebook).from(table)
      .where(QueryBuilder.eq(WorkflowId, workflowId.value))
      .and(QueryBuilder.eq(NodeId, nodeId.value))
  }

  private def getAllQuery(workflowId: Workflow.Id): Select.Where = {
    queryBuilder.select(NodeId, Notebook).from(table)
      .where(QueryBuilder.eq(WorkflowId, workflowId.value))
  }

  private def updateQuery(
      workflowId: Workflow.Id,
      nodeId: Node.Id,
      notebook: String): Update.Where = {
    queryBuilder.update(table).`with`()
      .and(set(Notebook, notebook))
      .where(QueryBuilder.eq(WorkflowId, workflowId.value))
      .and(QueryBuilder.eq(NodeId, nodeId.value))
  }
}

object NotebookDaoCassandraImpl {
  val WorkflowId = "workflow_id"
  val NodeId = "node_id"
  val Notebook = "notebook"
}
