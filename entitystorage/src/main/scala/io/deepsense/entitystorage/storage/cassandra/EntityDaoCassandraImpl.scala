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
import com.datastax.driver.core.querybuilder.{QueryBuilder, Select, Update}
import com.google.inject.Inject
import com.google.inject.name.Named

import io.deepsense.entitystorage.storage.EntityDao
import io.deepsense.models.entities.Entity

class EntityDaoCassandraImpl @Inject() (
    @Named("cassandra.entities.table") table: String,
    @Named("EntitiesSession") session: Session)
    (implicit ec: ExecutionContext)
  extends EntityDao {

  override def getAll(tenantId: String): Future[List[Entity]] =
    Future(session.execute(getAllQuery(tenantId)))
      .map(_.all().toList.map(EntityRowMapper.fromRow))

  override def get(tenantId: String, id: Entity.Id): Future[Option[Entity]] = {
    Future(session.execute(getQuery(tenantId, id)))
      .map(rs => Option(rs.one()).map(EntityRowMapper.fromRow))
  }

  override def upsert(entity: Entity): Future[Unit] = {
    Future(session.execute(upsertQuery(entity)))
  }

  override def delete(tenantId: String, id: Entity.Id): Future[Unit] = {
    Future(session.execute(deleteQuery(tenantId, id)))
  }

  private def getAllQuery(tenantId: String): Where = {
    QueryBuilder
      .select()
      .from(table)
      .where(QueryBuilder.eq(EntityRowMapper.TenantId, tenantId))
  }

  private def getQuery(tenantId: String, id: Entity.Id): Select = {
    QueryBuilder.select().from(table)
      .where(QueryBuilder.eq(EntityRowMapper.TenantId, tenantId))
      .and(QueryBuilder.eq(EntityRowMapper.Id, id.value)).limit(1)
  }

  private def upsertQuery(entity: Entity): Update.Where = {
    val update = QueryBuilder.update(table)
      .`with`(set(EntityRowMapper.Name, entity.name))
      .and(set(EntityRowMapper.Description, entity.description))
      .and(set(EntityRowMapper.DClass, entity.dClass))
      .and(set(EntityRowMapper.Url, entity.data.map(_.url).orNull))
      .and(set(EntityRowMapper.Created, entity.created.getMillis))
      .and(set(EntityRowMapper.Updated, entity.updated.getMillis))
      .and(set(EntityRowMapper.Saved, entity.saved))
    upsertReport(update, entity)
      .where(QueryBuilder.eq(EntityRowMapper.TenantId, entity.tenantId))
      .and(QueryBuilder.eq(EntityRowMapper.Id, entity.id.value))
  }

  private def upsertReport(update: Update.Assignments, entity: Entity): Assignments = {
    entity.report match {
      case Some(report) =>
        update.and(set(EntityRowMapper.Report, report.jsonReport))
      case None =>
        update
    }
  }

  private def deleteQuery(tenantId: String, id: Entity.Id) = {
    QueryBuilder.delete().from(table)
      .where(QueryBuilder.eq(EntityRowMapper.TenantId, tenantId))
      .and(QueryBuilder.eq(EntityRowMapper.Id, id.value))
  }
}
