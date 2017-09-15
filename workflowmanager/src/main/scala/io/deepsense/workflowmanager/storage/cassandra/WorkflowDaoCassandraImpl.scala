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
import io.deepsense.models.workflows.Workflow
import io.deepsense.workflowmanager.storage.{WorkflowWithDates, WorkflowStorage}

class WorkflowDaoCassandraImpl @Inject() (
    @Named("cassandra.workflowmanager.workflow.table") table: String,
    @Named("WorkflowsSession") session: Session,
    workflowRowMapper: WorkflowRowMapper)
    (implicit ec: ExecutionContext)
  extends WorkflowStorage  {

  private val queryBuilder = new QueryBuilder(session.getCluster)

  override def get(id: Workflow.Id): Future[Option[Workflow]] = {
    Future(session.execute(getWorkflowQuery(id)))
      .map(rs => Option(rs.one()).map(workflowRowMapper.toWorkflow))
  }

  override def delete(id: Workflow.Id): Future[Unit] = {
    Future(session.execute(deleteQuery(id)))
  }

  override def create(id: Workflow.Id, workflow: Workflow): Future[Unit] = {
    Future(session.execute(createWorkflowQuery(id, workflow)))
  }

  override def update(id: Workflow.Id, workflow: Workflow): Future[Unit] = {
    Future(session.execute(updateWorkflowQuery(id, workflow)))
  }

  override def getAll(): Future[Map[Workflow.Id, WorkflowWithDates]] = {
    import scala.collection.JavaConversions._
    Future(session.execute(getAllWorkflowsQuery))
      .map(_.all().map(row =>
        Workflow.Id.fromUuid(row.getUUID(WorkflowRowMapper.Id))
          -> workflowRowMapper.toWorkflowWithDates(row)
    ).toMap)
  }

  private def getWorkflowQuery(id: Workflow.Id): Select = {
    getQuery(id, Seq(WorkflowRowMapper.Id, WorkflowRowMapper.Workflow))
  }

  private def getAllWorkflowsQuery: Select.Where = {
    queryBuilder.select(
      WorkflowRowMapper.Id,
      WorkflowRowMapper.Workflow,
      WorkflowRowMapper.Created,
      WorkflowRowMapper.Updated)
      .from(table)
      .where(QueryBuilder.eq(WorkflowRowMapper.Deleted, false))
  }

  private def getQuery(id: Workflow.Id, columns: Seq[String]): Select = {
    queryBuilder
      .select(columns: _*)
      .from(table)
      .where(QueryBuilder.eq(WorkflowRowMapper.Id, id.value))
      .and(QueryBuilder.eq(WorkflowRowMapper.Deleted, false))
      .limit(1)
  }

  private def createWorkflowQuery(id: Workflow.Id, workflow: Workflow): Update.Where = {
    updateQuery(id, Seq(
      (WorkflowRowMapper.Workflow, workflowRowMapper.workflowToCell(workflow)),
      (WorkflowRowMapper.Created, DateTimeConverter.now.getMillis),
      (WorkflowRowMapper.Updated, DateTimeConverter.now.getMillis)
    ))
  }

  private def updateWorkflowQuery(id: Workflow.Id, workflow: Workflow): Update.Where = {
    updateQuery(id, Seq(
      (WorkflowRowMapper.Workflow, workflowRowMapper.workflowToCell(workflow)),
      (WorkflowRowMapper.Updated, DateTimeConverter.now.getMillis)
    ))
  }

  private def updateQuery(id: Id, valuesToSet: Seq[(String, Any)]): Update.Where = {
    val queryWithAssignments =
      valuesToSet.foldLeft(queryBuilder.update(table).`with`()) {
        case (assignments, (field, value)) => assignments.and(set(field, value))
      }

    queryWithAssignments
      .and(set(WorkflowRowMapper.Deleted, false))
      .where(QueryBuilder.eq(WorkflowRowMapper.Id, id.value))
  }

  private def deleteQuery(id: Workflow.Id): Update.Where = {
    queryBuilder
      .update(table)
      .`with`(set(WorkflowRowMapper.Deleted, true))
      .where(QueryBuilder.eq(WorkflowRowMapper.Id, id.value))
  }
}
