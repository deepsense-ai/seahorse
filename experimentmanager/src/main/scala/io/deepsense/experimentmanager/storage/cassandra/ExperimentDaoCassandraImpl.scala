/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.experimentmanager.storage.cassandra


import scala.collection.JavaConversions._
import scala.concurrent.{ExecutionContext, Future}

import com.datastax.driver.core.Session
import com.datastax.driver.core.querybuilder.QueryBuilder._
import com.datastax.driver.core.querybuilder.Select.Where
import com.datastax.driver.core.querybuilder.{Delete, QueryBuilder, Select, Update}
import com.google.inject.Inject
import com.google.inject.name.Named
import spray.json._

import io.deepsense.experimentmanager.storage.ExperimentStorage
import io.deepsense.models.entities.Entity
import io.deepsense.models.entities.Entity.Id
import io.deepsense.models.experiments.Experiment
import io.deepsense.graphjson.GraphJsonProtocol.GraphWriter
import io.deepsense.commons.exception.json.FailureDescriptionJsonProtocol

class ExperimentDaoCassandraImpl @Inject() (
    @Named("cassandra.experiments.table") table: String,
    @Named("ExperimentsSession") session: Session,
    experimentRowMapper: ExperimentRowMapper)
    (implicit ec: ExecutionContext)
  extends ExperimentStorage with FailureDescriptionJsonProtocol {

  override def get(tenantId: String, id: Entity.Id): Future[Option[Experiment]] = {
    Future(session.execute(getQuery(tenantId, id)))
      .map(rs => Option(rs.one()).map(experimentRowMapper.fromRow))
  }

  override def delete(tenantId: String, id: Id): Future[Unit] = {
    Future(session.execute(deleteQuery(tenantId, id)))
  }

  override def save(experiment: Experiment): Future[Unit] = {
    Future(session.execute(upsertQuery(experiment)))
  }

  override def list(
    tenantId: String,
    limit: Option[Int] = None,
    page: Option[Int] = None,
    status: Option[Experiment.Status.Value] = None): Future[Seq[Experiment]] = {
    Future(session.execute(getAllQuery(tenantId)))
      .map(_.all().toList.map(experimentRowMapper.fromRow))
  }

  private def getAllQuery(tenantId: String): Where = {
    QueryBuilder.select()
      .from(table)
      .where(QueryBuilder.eq(ExperimentRowMapper.TenantId, tenantId))
      .and(QueryBuilder.eq(ExperimentRowMapper.Deleted, false))
  }

  private def getQuery(tenantId: String, id: Entity.Id): Select = {
    QueryBuilder.select().from(table)
      .where(QueryBuilder.eq(ExperimentRowMapper.TenantId, tenantId))
      .and(QueryBuilder.eq(ExperimentRowMapper.Id, id.value))
      .and(QueryBuilder.eq(ExperimentRowMapper.Deleted, false))
      .limit(1)
  }

  private def upsertQuery(experiment: Experiment): Update.Where = {
    val update = QueryBuilder.update(table)
      .`with`(set(ExperimentRowMapper.Name, experiment.name))
      .and(set(ExperimentRowMapper.Description, experiment.description))
      .and(set(ExperimentRowMapper.Graph, experiment.graph.toJson.toString))
      .and(set(ExperimentRowMapper.Updated, experiment.updated.getMillis))
      .and(set(ExperimentRowMapper.Created, experiment.created.getMillis))
      .and(set(ExperimentRowMapper.StateStatus, experiment.state.status.toString))
      .and(set(ExperimentRowMapper.Deleted, false))
    experiment.state.error match {
      case Some(state) => {
        val stateDescription = state.toJson.toString
        update.and(set(ExperimentRowMapper.StateDescription, stateDescription))
      }
      case _ =>
    }

    update.where(QueryBuilder.eq(ExperimentRowMapper.TenantId, experiment.tenantId))
      .and(QueryBuilder.eq(ExperimentRowMapper.Id, experiment.id.value))
  }

  private def deleteQuery(tenantId: String, id: Entity.Id): Update.Where = {
    QueryBuilder.update(table)
      .`with`(set(ExperimentRowMapper.Deleted, true))
      .where(QueryBuilder.eq(ExperimentRowMapper.TenantId, tenantId))
      .and(QueryBuilder.eq(ExperimentRowMapper.Id, id.value))
  }
}
