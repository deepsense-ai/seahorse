/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.workflowmanager.storage.cassandra

import scala.concurrent.{ExecutionContext, Future}

import com.datastax.driver.core.Session
import com.datastax.driver.core.querybuilder.QueryBuilder._
import com.datastax.driver.core.querybuilder.{QueryBuilder, Select, Update}
import com.google.inject.Inject
import com.google.inject.name.Named

import io.deepsense.commons.models.Id
import io.deepsense.models.workflows.{Workflow, WorkflowWithSavedResults}
import io.deepsense.workflowmanager.storage.WorkflowStorage

class WorkflowDaoCassandraImpl @Inject() (
    @Named("cassandra.workflowmanager.workflow.table") table: String,
    @Named("WorkflowsSession") session: Session,
    workflowRowMapper: WorkflowRowMapper)
    (implicit ec: ExecutionContext)
  extends WorkflowStorage  {

  override def get(id: Workflow.Id): Future[Option[Workflow]] = {
    Future(session.execute(getWorkflowQuery(id)))
      .map(rs => Option(rs.one()).map(workflowRowMapper.toWorkflow))
  }

  override def delete(id: Workflow.Id): Future[Unit] = {
    Future(session.execute(deleteQuery(id)))
  }

  override def save(id: Workflow.Id, workflow: Workflow): Future[Unit] = {
    Future(session.execute(saveWorkflowQuery(id, workflow)))
  }

  override def getLatestExecutionResults(
      workflowId: Id): Future[Option[WorkflowWithSavedResults]] = {
    Future(session.execute(getResultsQuery(workflowId)))
      .map(rs => Option(rs.one()).flatMap(workflowRowMapper.toWorkflowWithSavedResults))
  }

  override def saveExecutionResults(results: WorkflowWithSavedResults): Future[Unit] = {
    Future(session.execute(saveResultsQuery(results)))
  }

  private def getWorkflowQuery(id: Workflow.Id): Select = {
    getQuery(id, Seq(WorkflowRowMapper.Id, WorkflowRowMapper.Workflow))
  }

  private def getResultsQuery(id: Workflow.Id): Select = {
    getQuery(id, Seq(WorkflowRowMapper.Id, WorkflowRowMapper.Results))
  }

  private def getQuery(id: Workflow.Id, columns: Seq[String]): Select = {
    QueryBuilder.select(columns: _*).from(table)
      .where(QueryBuilder.eq(WorkflowRowMapper.Id, id.value))
      .and(QueryBuilder.eq(WorkflowRowMapper.Deleted, false))
      .limit(1)
  }

  private def saveWorkflowQuery(id: Workflow.Id, workflow: Workflow): Update.Where = {
    updateQuery(id, (WorkflowRowMapper.Workflow, workflowRowMapper.workflowToCell(workflow)))
  }

  private def saveResultsQuery(results: WorkflowWithSavedResults): Update.Where = {
    updateQuery(results.id, (WorkflowRowMapper.Results, workflowRowMapper.resultsToCell(results)))
  }

  private def updateQuery(id: Id, valueToSet: (String, String)): Update.Where = {
    QueryBuilder.update(table)
      .`with`(set(valueToSet._1, valueToSet._2))
      .and(set(WorkflowRowMapper.Deleted, false))
      .where(QueryBuilder.eq(WorkflowRowMapper.Id, id.value))
  }

  private def deleteQuery(id: Workflow.Id): Update.Where = {
    QueryBuilder.update(table)
      .`with`(set(WorkflowRowMapper.Deleted, true))
      .where(QueryBuilder.eq(WorkflowRowMapper.Id, id.value))
  }
}
