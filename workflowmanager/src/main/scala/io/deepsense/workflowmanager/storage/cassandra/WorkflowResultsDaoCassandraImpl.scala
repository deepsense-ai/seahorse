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

import io.deepsense.models.workflows.{WorkflowWithSavedResults, ExecutionReportWithId}
import io.deepsense.workflowmanager.storage.WorkflowResultsStorage

class WorkflowResultsDaoCassandraImpl @Inject() (
    @Named("cassandra.workflowmanager.workflowresults.table") table: String,
    @Named("WorkflowsSession") session: Session,
    workflowResultsRowMapper: WorkflowResultsRowMapper)
    (implicit ec: ExecutionContext)
  extends WorkflowResultsStorage {

  override def get(id: ExecutionReportWithId.Id): Future[Option[WorkflowWithSavedResults]] = {
    Future(session.execute(getQuery(id)))
      .map(rs => Option(rs.one()).map(workflowResultsRowMapper.fromRow))
  }

  override def save(results: WorkflowWithSavedResults): Future[Unit] = {
    Future(session.execute(saveQuery(results)))
  }

  private def getQuery(id: ExecutionReportWithId.Id): Select = {
    QueryBuilder.select().from(table)
      .where(QueryBuilder.eq(WorkflowResultsRowMapper.Id, id.value))
      .limit(1)
  }

  private def saveQuery(results: WorkflowWithSavedResults): Update.Where = {
    QueryBuilder.update(table)
      .`with`(set(WorkflowResultsRowMapper.Results, workflowResultsRowMapper.resultToCell(results)))
      .where(QueryBuilder.eq(WorkflowResultsRowMapper.Id, results.executionReport.id.value))
  }
}
