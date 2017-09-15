/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.workflowmanager.storage.cassandra

import scala.collection.JavaConversions._
import scala.concurrent.{ExecutionContext, Future}

import com.datastax.driver.core.Session
import com.datastax.driver.core.querybuilder.QueryBuilder._
import com.datastax.driver.core.querybuilder.Update.Assignments
import com.datastax.driver.core.querybuilder.{QueryBuilder, Update}
import com.google.inject.Inject
import com.google.inject.name.Named

import io.deepsense.commons.datetime.DateTimeConverter
import io.deepsense.graph.Node
import io.deepsense.models.workflows.Workflow.Id
import io.deepsense.workflowmanager.storage.WorkflowStateStorage
import io.deepsense.workflowmanager.storage.WorkflowStateStorage.NodeStateWithReports

class WorkflowStateDaoCassandraImpl @Inject() (
    @Named("cassandra.workflowmanager.workflowstate.table") table: String,
    @Named("WorkflowsSession") session: Session,
    rowMapper: WorkflowStateRowMapper)
    (implicit ec: ExecutionContext) extends WorkflowStateStorage {

  import WorkflowStateRowMapper._

  private val queryBuilder = new QueryBuilder(session.getCluster)

  override def get(workflowId: Id): Future[Map[Node.Id, NodeStateWithReports]] = {
    val query = queryBuilder
      .select(Field.NodeId, Field.State, Field.Reports)
      .from(table)
      .where(QueryBuilder.eq(Field.WorkflowId, workflowId.value))

    Future(session.execute(query))
      .map(rs => rs.all().toList.map(rowMapper.toIdAndNodeStateWithReports).toMap)
  }

  override def save(
      workflowId: Id,
      state: Map[Node.Id, NodeStateWithReports]): Future[Unit] = {

    def query(nodeId: Node.Id, nodeStateWithReports: NodeStateWithReports): Update.Where =
      queryBuilder.update(table).`with`()
        .and(Field.State, rowMapper.nodeStateToCell(nodeStateWithReports.nodeState))
        .and(Field.UpdateTime, DateTimeConverter.now.getMillis)
        .andOptionally(Field.Reports, nodeStateWithReports.reports map rowMapper.entitiesMapToCell)
        .where(QueryBuilder.eq(Field.WorkflowId, workflowId.value))
        .and(QueryBuilder.eq(Field.NodeId, nodeId.value))

    val futures = state.map {
      case (nodeId, nodeState) => Future(session.execute(query(nodeId, nodeState)))
    }

    reduced(futures)
  }

  private def reduced[A](futures: Iterable[Future[A]]): Future[Unit] =
    if (futures.isEmpty) {
      Future.successful(())
    } else {
      val unitFutures = futures.map( _.map( a => () ) )
      Future.reduce(unitFutures)((x, y) => ())
    }

  private implicit class SimpleAssignments(val assignments: Assignments) {
    def and(key: String, value: Any): Assignments =
      assignments.and(set(key, value))

    def andOptionally(key: String, valueOpt: Option[String]): Assignments =
      valueOpt match {
        case None => assignments
        case Some(value) => and(key, value)
      }
  }

}
