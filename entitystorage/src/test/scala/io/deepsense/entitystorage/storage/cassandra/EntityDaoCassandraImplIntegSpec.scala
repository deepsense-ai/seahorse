/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Wojciech Jurczyk
 */

package io.deepsense.entitystorage.storage.cassandra

import scala.concurrent.{Await, Future}

import com.datastax.driver.core.querybuilder.QueryBuilder
import org.scalatest._
import org.scalatest.concurrent.ScalaFutures

import io.deepsense.commons.StandardSpec
import io.deepsense.commons.datetime.DateTimeConverter
import io.deepsense.commons.models.Id
import io.deepsense.deeplang.doperables.Report
import io.deepsense.entitystorage.CassandraTestSupport
import io.deepsense.entitystorage.models.{DataObject, DataObjectReference, DataObjectReport, Entity}

class EntityDaoCassandraImplIntegSpec
  extends StandardSpec
  with ScalaFutures
  with Matchers
  with BeforeAndAfter
  with CassandraTestSupport {

  var entities: EntityDaoCassandraImpl = _

  before {
    session.execute(createTableCommand(table))
    entities = new EntityDaoCassandraImpl(table, session)
  }

  // Fixture
  val tenantId = "TestTenantId"
  val created = DateTimeConverter.now
  val updated = created.plusDays(5)
  val dataObjectReference = DataObjectReference("http://example.com")
  val dataObjectReport = DataObjectReport(Report())

  val entity1 = createEntity(tenantId, 3, dataObjectReference)
  val entity2 = createEntity(tenantId, 14, dataObjectReport)
  val entity3 = createEntity(tenantId + "otherTenant", 15, dataObjectReport)
  val inDb = Set(entity1, entity2, entity3)

  "Entities" should {
    "select all rows from a tenant" in withStoredEntities(inDb) {
      whenReady(entities.getAll(tenantId)) { tenantEntities =>
        tenantEntities should contain theSameElementsAs Seq(entity1, entity2)
      }
    }
    "save and get an entity" that {
      val tenantId = "save_get_tenantId"
      "is a report" in {
        val entity = createEntity(tenantId, 0, dataObjectReport)
        whenReady(entities.upsert(entity)) { _ =>
          whenReady(entities.get(tenantId, entity.id)) { getEntity =>
            getEntity shouldBe Some(entity)
          }
        }
      }
      "is an url" in {
        val entity = createEntity(tenantId, 0, dataObjectReference)
        whenReady(entities.upsert(entity)) { _ =>
          whenReady(entities.get(tenantId, entity.id)) { getEntity =>
            getEntity shouldBe Some(entity)
          }
        }
      }
    }
    "update an entity" in withStoredEntities(inDb) {
      val differentEntity = modifyEntity(entity1)
      whenReady(entities.upsert(differentEntity)) { _ =>
        whenReady(entities.get(tenantId, entity1.id)) { getEntity =>
          getEntity shouldBe Some(differentEntity)
        }
      }
    }
    "delete an entity" in {
      whenReady(entities.delete(tenantId, entity1.id)) { _ =>
        whenReady(entities.get(tenantId, entity1.id)) { getEntity =>
          getEntity shouldBe None
        }
      }
    }
  }

  private def createEntity(
      tenantId: String,
      number: Int,
      dataObject: DataObject): Entity = {
    Entity(
      tenantId,
      Id.randomId,
      "name" + number,
      "desc" + number,
      "dclass" + number,
      created.plusDays(number),
      updated.plusDays(number),
      dataObject,
      number % 2 == 1)
  }

  private def modifyEntity(entity: Entity): Entity = {
    val s = "modified"
    entity.copy(
      name = entity.name + s,
      description = entity.description + s,
      dClass = entity.dClass + s,
      created = entity.created.plusDays(1),
      updated = entity.updated.plusDays(1),
      data = DataObjectReference(entity.name + entity.description),
      saved = !entity.saved)
  }

  private def withStoredEntities(storedEntities: Set[Entity])(testCode: => Any): Unit = {
    val s = Future.sequence(storedEntities.map(entities.upsert))
    Await.ready(s, operationDuration)
    try {
      testCode
    } finally {
      session.execute(QueryBuilder.truncate(table))
    }
  }

  private def createTableCommand(table: String): String = {
    s"create table if not exists $table (" +
    """
      tenantid text,
      id uuid,
      name text,
      description text,
      dclass text,
      created timestamp,
      updated timestamp,
      url text,
      saved boolean,
      primary key (tenantid, id)
      );
    """
  }
}
