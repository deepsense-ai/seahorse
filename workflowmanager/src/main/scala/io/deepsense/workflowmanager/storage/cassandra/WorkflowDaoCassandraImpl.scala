/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.workflowmanager.storage.cassandra


import scala.collection.JavaConversions._
import scala.concurrent.{ExecutionContext, Future}

import com.datastax.driver.core.Session
import com.datastax.driver.core.querybuilder.QueryBuilder._
import com.datastax.driver.core.querybuilder.Select.Where
import com.datastax.driver.core.querybuilder.{QueryBuilder, Select, Update}
import com.google.inject.Inject
import com.google.inject.name.Named
import spray.json._

import io.deepsense.commons.exception.json.FailureDescriptionJsonProtocol
import io.deepsense.model.json.graph.GraphJsonProtocol
import GraphJsonProtocol.GraphWriter
import io.deepsense.models.entities.Entity
import io.deepsense.models.workflows.Workflow
import io.deepsense.workflowmanager.storage.WorkflowStorage

class WorkflowDaoCassandraImpl @Inject() (
    @Named("cassandra.experiments.table") table: String,
    @Named("ExperimentsSession") session: Session,
    experimentRowMapper: WorkflowRowMapper)
    (implicit ec: ExecutionContext)
  extends WorkflowStorage with FailureDescriptionJsonProtocol {

  override def get(tenantId: String, id: Entity.Id): Future[Option[Workflow]] = {
    Future(session.execute(getQuery(tenantId, id)))
      .map(rs => Option(rs.one()).map(experimentRowMapper.fromRow))
  }

  override def delete(tenantId: String, id: Entity.Id): Future[Unit] = {
    Future(session.execute(deleteQuery(tenantId, id)))
  }

  override def save(experiment: Workflow): Future[Unit] = {
    Future(session.execute(upsertQuery(experiment)))
  }

  override def list(
    tenantId: String,
    limit: Option[Int] = None,
    page: Option[Int] = None,
    status: Option[Workflow.Status.Value] = None): Future[Seq[Workflow]] = {
    Future(session.execute(getAllQuery(tenantId)))
      .map(_.all().toList.map(experimentRowMapper.fromRow))
  }

  private def getAllQuery(tenantId: String): Where = {
    QueryBuilder.select()
      .from(table)
      .where(QueryBuilder.eq(WorkflowRowMapper.TenantId, tenantId))
      .and(QueryBuilder.eq(WorkflowRowMapper.Deleted, false))
  }

  private def getQuery(tenantId: String, id: Entity.Id): Select = {
    QueryBuilder.select().from(table)
      .where(QueryBuilder.eq(WorkflowRowMapper.TenantId, tenantId))
      .and(QueryBuilder.eq(WorkflowRowMapper.Id, id.value))
      .and(QueryBuilder.eq(WorkflowRowMapper.Deleted, false))
      .limit(1)
  }

  private def upsertQuery(experiment: Workflow): Update.Where = {
    val update = QueryBuilder.update(table)
      .`with`(set(WorkflowRowMapper.Name, experiment.name))
      .and(set(WorkflowRowMapper.Description, experiment.description))
      .and(set(WorkflowRowMapper.Graph, experiment.graph.toJson.toString))
      .and(set(WorkflowRowMapper.Updated, experiment.updated.getMillis))
      .and(set(WorkflowRowMapper.Created, experiment.created.getMillis))
      .and(set(WorkflowRowMapper.StateStatus, experiment.state.status.toString))
      .and(set(WorkflowRowMapper.Deleted, false))
    experiment.state.error match {
      case Some(state) => {
        val stateDescription = state.toJson.toString
        update.and(set(WorkflowRowMapper.StateDescription, stateDescription))
      }
      case _ =>
    }

    update.where(QueryBuilder.eq(WorkflowRowMapper.TenantId, experiment.tenantId))
      .and(QueryBuilder.eq(WorkflowRowMapper.Id, experiment.id.value))
  }

  private def deleteQuery(tenantId: String, id: Entity.Id): Update.Where = {
    QueryBuilder.update(table)
      .`with`(set(WorkflowRowMapper.Deleted, true))
      .where(QueryBuilder.eq(WorkflowRowMapper.TenantId, tenantId))
      .and(QueryBuilder.eq(WorkflowRowMapper.Id, id.value))
  }
}
