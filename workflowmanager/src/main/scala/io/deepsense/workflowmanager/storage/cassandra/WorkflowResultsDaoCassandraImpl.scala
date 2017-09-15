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

import io.deepsense.models.workflows.{ExecutionReportWithId, WorkflowWithSavedResults}
import io.deepsense.workflowmanager.storage.WorkflowResultsStorage

class WorkflowResultsDaoCassandraImpl @Inject() (
    @Named("cassandra.workflowmanager.workflowresults.table") table: String,
    @Named("WorkflowsSession") session: Session,
    workflowRowMapper: WorkflowRowMapper)
    (implicit ec: ExecutionContext)
  extends WorkflowResultsStorage {

  private val queryBuilder = new QueryBuilder(session.getCluster)

  override def get(
      id: ExecutionReportWithId.Id): Future[Option[Either[String, WorkflowWithSavedResults]]] = {
    Future(session.execute(getQuery(id)))
      .map(rs => Option(rs.one()).flatMap(workflowRowMapper.toWorkflowWithSavedResults))
  }

  override def save(results: WorkflowWithSavedResults): Future[Unit] = {
    Future(session.execute(saveQuery(results)))
  }

  private def getQuery(id: ExecutionReportWithId.Id): Select = {
    queryBuilder
      .select()
      .from(table)
      .where(QueryBuilder.eq(WorkflowRowMapper.Id, id.value))
      .limit(1)
  }

  private def saveQuery(results: WorkflowWithSavedResults): Update.Where = {
    queryBuilder
      .update(table)
      .`with`(set(WorkflowRowMapper.Results, workflowRowMapper.resultsToCell(results)))
      .where(QueryBuilder.eq(WorkflowRowMapper.Id, results.executionReport.id.value))
  }
}
