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

import io.deepsense.models.workflows.Workflow
import io.deepsense.workflowmanager.storage.WorkflowStorage

class WorkflowDaoCassandraImpl @Inject() (
    @Named("cassandra.workflow.table") table: String,
    @Named("WorkflowsSession") session: Session,
    workflowRowMapper: WorkflowRowMapper)
    (implicit ec: ExecutionContext)
  extends WorkflowStorage  {

  override def get(id: Workflow.Id): Future[Option[Workflow]] = {
    Future(session.execute(getQuery(id)))
      .map(rs => Option(rs.one()).map(workflowRowMapper.fromRow))
  }

  override def delete(id: Workflow.Id): Future[Unit] = {
    Future(session.execute(deleteQuery(id)))
  }

  override def save(id: Workflow.Id, workflow: Workflow): Future[Unit] = {
    Future(session.execute(upsertQuery(id, workflow)))
  }

  private def getQuery(id: Workflow.Id): Select = {
    QueryBuilder.select().from(table)
      .where(QueryBuilder.eq(WorkflowRowMapper.Id, id.value))
      .and(QueryBuilder.eq(WorkflowRowMapper.Deleted, false))
      .limit(1)
  }

  private def upsertQuery(id: Workflow.Id, workflow: Workflow): Update.Where = {
    val update = QueryBuilder.update(table)
      .`with`(set(WorkflowRowMapper.Workflow, workflowRowMapper.workflowToCell(workflow)))
      .and(set(WorkflowRowMapper.Deleted, false))
    update.where(QueryBuilder.eq(WorkflowRowMapper.Id, id.value))
  }

  private def deleteQuery(id: Workflow.Id): Update.Where = {
    QueryBuilder.update(table)
      .`with`(set(WorkflowRowMapper.Deleted, true))
      .where(QueryBuilder.eq(WorkflowRowMapper.Id, id.value))
  }
}
