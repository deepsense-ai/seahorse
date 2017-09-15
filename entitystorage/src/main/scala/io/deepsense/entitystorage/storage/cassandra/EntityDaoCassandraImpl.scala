/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.entitystorage.storage.cassandra

import scala.collection.JavaConversions._
import scala.concurrent.{ExecutionContext, Future}

import com.datastax.driver.core.Session
import com.datastax.driver.core.querybuilder.QueryBuilder._
import com.datastax.driver.core.querybuilder.Select.Where
import com.datastax.driver.core.querybuilder.Update.Assignments
import com.datastax.driver.core.querybuilder.{Delete, QueryBuilder, Select, Update}
import com.google.inject.Inject
import com.google.inject.name.Named
import org.joda.time.DateTime

import io.deepsense.entitystorage.storage.EntityDao
import io.deepsense.models.entities._

class EntityDaoCassandraImpl @Inject() (
    @Named("cassandra.entities.table") table: String,
    @Named("EntitiesSession") session: Session)
    (implicit ec: ExecutionContext)
  extends EntityDao {

  override def getAll(tenantId: String): Future[List[EntityInfo]] = {
    Future(session.execute(
      getAllQuery(tenantId, selectedFields = EntityRowMapper.EntityInfoFields))
    ).map(_.all().toList.map(EntityRowMapper.toEntityInfo))
  }

  override def getWithData(tenantId: String, id: Entity.Id): Future[Option[EntityWithData]] = {
    Future(session.execute(
      getQuery(tenantId, id, selectedFields = EntityRowMapper.EntityWithDataFields))
    ).map(rs => {
      println(rs)
      Option(rs.one()).map(EntityRowMapper.toEntityWithData)
    })
  }

  override def getWithReport(tenantId: String, id: Entity.Id): Future[Option[EntityWithReport]] = {
    Future(session.execute(
      getQuery(tenantId, id, selectedFields = EntityRowMapper.EntityWithReportFields))
    ).map(rs => Option(rs.one()).map(EntityRowMapper.toEntityWithReport))
  }

  override def create(id: Entity.Id, entity: EntityCreate, created: DateTime): Future[Unit] = {
    Future(session.execute(createQuery(id, entity, created)))
  }

  def update(
      tenantId: String,
      id: Entity.Id,
      entity: EntityUpdate,
      updated: DateTime): Future[Unit] = {
    Future(session.execute(updateEntityQuery(tenantId, id, entity, updated)))
  }

  override def delete(tenantId: String, id: Entity.Id): Future[Unit] = {
    Future(session.execute(deleteQuery(tenantId, id)))
  }

  private def getAllQuery(tenantId: String, selectedFields: Seq[String]): Where = {
    QueryBuilder
      .select(selectedFields: _*)
      .from(table)
      .where(QueryBuilder.eq(EntityRowMapper.TenantId, tenantId))
  }

  private def getQuery(tenantId: String, id: Entity.Id, selectedFields: Seq[String]): Select = {
    QueryBuilder.select(selectedFields: _*).from(table)
      .where(QueryBuilder.eq(EntityRowMapper.TenantId, tenantId))
      .and(QueryBuilder.eq(EntityRowMapper.Id, id.value)).limit(1)
  }

  private def createQuery(
      id: Entity.Id,
      entity: EntityCreate,
      created: DateTime): Update.Where = {
    inputQuery(entity)
      .and(set(EntityRowMapper.DClass, entity.dClass))
      .and(set(EntityRowMapper.Url, entity.dataReference.map(_.url).orNull))
      .and(set(EntityRowMapper.Created, created.getMillis))
      .and(set(EntityRowMapper.Updated, created.getMillis))
      .and(set(EntityRowMapper.Report, entity.report.jsonReport))
      .where(QueryBuilder.eq(EntityRowMapper.TenantId, entity.tenantId))
      .and(QueryBuilder.eq(EntityRowMapper.Id, id.value))
  }

  private def updateEntityQuery(
      tenantId: String,
      id: Entity.Id,
      entity: EntityUpdate,
      updated: DateTime): Update.Where = {

    inputQuery(entity)
      .and(set(EntityRowMapper.Updated, updated.getMillis))
      .where(QueryBuilder.eq(EntityRowMapper.TenantId, tenantId))
      .and(QueryBuilder.eq(EntityRowMapper.Id, id.value))
  }

  private def inputQuery(entity: InputEntityFields): Update.Assignments = {
    QueryBuilder.update(table)
      .`with`(set(EntityRowMapper.Name, entity.name))
      .and(set(EntityRowMapper.Description, entity.description))
      .and(set(EntityRowMapper.Saved, entity.saved))
  }

  private def deleteQuery(tenantId: String, id: Entity.Id): Delete.Where = {
    QueryBuilder.delete().from(table)
      .where(QueryBuilder.eq(EntityRowMapper.TenantId, tenantId))
      .and(QueryBuilder.eq(EntityRowMapper.Id, id.value))
  }
}
