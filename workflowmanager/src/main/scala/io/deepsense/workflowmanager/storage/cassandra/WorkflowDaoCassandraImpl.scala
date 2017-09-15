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
import org.joda.time.DateTime

import io.deepsense.commons.datetime.DateTimeConverter
import io.deepsense.commons.models.Id
import io.deepsense.models.workflows.{Workflow, WorkflowWithSavedResults}
import io.deepsense.workflowmanager.storage.WorkflowStorage

class WorkflowDaoCassandraImpl @Inject() (
    @Named("cassandra.workflowmanager.workflow.table") table: String,
    @Named("WorkflowsSession") session: Session,
    workflowRowMapper: WorkflowRowMapper)
    (implicit ec: ExecutionContext)
  extends WorkflowStorage  {

  override def get(id: Workflow.Id): Future[Option[Either[String, Workflow]]] = {
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
      workflowId: Id): Future[Option[Either[String, WorkflowWithSavedResults]]] = {
    Future(session.execute(getResultsQuery(workflowId)))
      .map(rs => Option(rs.one()).flatMap(workflowRowMapper.toWorkflowWithSavedResults))
  }

  override def saveExecutionResults(results: WorkflowWithSavedResults): Future[Unit] = {
    Future(session.execute(saveResultsQuery(results, DateTimeConverter.now)))
  }

  override def getResultsUploadTime(workflowId: Id): Future[Option[DateTime]] = {
    Future(
      session.execute(getResultsUploadTimeQuery(workflowId)))
      .map(rs => Option(rs.one()).flatMap(workflowRowMapper.toResultsUploadTime))
  }

  private def getWorkflowQuery(id: Workflow.Id): Select = {
    getQuery(id, Seq(WorkflowRowMapper.Id, WorkflowRowMapper.Workflow))
  }

  private def getResultsQuery(id: Workflow.Id): Select = {
    getQuery(id, Seq(WorkflowRowMapper.Id, WorkflowRowMapper.Results))
  }

  private def getResultsUploadTimeQuery(id: Workflow.Id): Select =
    getQuery(id, Seq(WorkflowRowMapper.ResultsUploadTime))

  private def getQuery(id: Workflow.Id, columns: Seq[String]): Select = {
    QueryBuilder.select(columns: _*).from(table)
      .where(QueryBuilder.eq(WorkflowRowMapper.Id, id.value))
      .and(QueryBuilder.eq(WorkflowRowMapper.Deleted, false))
      .limit(1)
  }

  private def saveWorkflowQuery(id: Workflow.Id, workflow: Workflow): Update.Where = {
    updateQuery(id, Seq(
      (WorkflowRowMapper.Workflow, workflowRowMapper.workflowToCell(workflow))))
  }

  private def saveResultsQuery(
    results: WorkflowWithSavedResults,
    resultsUploadTime: DateTime): Update.Where = {
    updateQuery(results.id, Seq(
      (WorkflowRowMapper.Results, workflowRowMapper.resultsToCell(results)),
      (WorkflowRowMapper.ResultsUploadTime,
        workflowRowMapper.resultsUploadTimeToCell(resultsUploadTime))))
  }

  private def updateQuery(id: Id, valuesToSet: Seq[(String, Any)]): Update.Where = {
    val queryWithAssignments =
      valuesToSet.foldLeft(QueryBuilder.update(table).`with`()) {
        case (assignments, (field, value)) => assignments.and(set(field, value))
      }

    queryWithAssignments
      .and(set(WorkflowRowMapper.Deleted, false))
      .where(QueryBuilder.eq(WorkflowRowMapper.Id, id.value))
  }

  private def deleteQuery(id: Workflow.Id): Update.Where = {
    QueryBuilder.update(table)
      .`with`(set(WorkflowRowMapper.Deleted, true))
      .where(QueryBuilder.eq(WorkflowRowMapper.Id, id.value))
  }
}
