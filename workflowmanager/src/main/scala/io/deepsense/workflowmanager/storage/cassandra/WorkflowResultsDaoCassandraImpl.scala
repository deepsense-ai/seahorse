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
import io.deepsense.models.workflows.{Workflow, WorkflowWithResults}
import io.deepsense.workflowmanager.storage.WorkflowResultsStorage

class WorkflowResultsDaoCassandraImpl @Inject() (
    @Named("cassandra.workflowresults.table") table: String,
    @Named("WorkflowsSession") session: Session,
    workflowResultsRowMapper: WorkflowResultsRowMapper)
    (implicit ec: ExecutionContext)
  extends WorkflowResultsStorage {

  override def get(id: Workflow.Id): Future[List[WorkflowWithResults]] = {
    Future(session.execute(getQuery(id)))
      .map(rs => Option(rs.one()).map(workflowResultsRowMapper.fromRow).getOrElse(List()))
  }

  override def save(id: Id, results: WorkflowWithResults): Future[Unit] = {
    Future(session.execute(appendQuery(id, results)))
  }

  private def getQuery(id: Workflow.Id): Select = {
    QueryBuilder.select().from(table)
      .where(QueryBuilder.eq(WorkflowResultsRowMapper.Id, id.value))
      .limit(1)
  }

  private def appendQuery(id: Workflow.Id, results: WorkflowWithResults): Update.Where = {
    val update = QueryBuilder.update(table).`with`(append(
      WorkflowResultsRowMapper.Results, workflowResultsRowMapper.resultToCell(results)))
    update.where(QueryBuilder.eq(WorkflowResultsRowMapper.Id, id.value))
  }
}
