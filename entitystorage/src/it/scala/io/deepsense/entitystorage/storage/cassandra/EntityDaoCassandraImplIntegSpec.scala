/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.entitystorage.storage.cassandra

import scala.concurrent.{Await, Future}

import com.datastax.driver.core.querybuilder.QueryBuilder
import org.scalatest._
import org.scalatest.concurrent.ScalaFutures

import io.deepsense.commons.StandardSpec
import io.deepsense.commons.datetime.DateTimeConverter
import io.deepsense.entitystorage.EntitiesTableCreator
import io.deepsense.entitystorage.factories.EntityTestFactory
import io.deepsense.models.entities._
import io.deepsense.commons.cassandra.CassandraTestSupport

class EntityDaoCassandraImplIntegSpec
  extends StandardSpec
  with ScalaFutures
  with Matchers
  with BeforeAndAfter
  with CassandraTestSupport
  with EntityTestFactory {

  var entityDao: EntityDaoCassandraImpl = _

  def cassandraTableName : String = "entities"
  def cassandraKeySpaceName : String = "entitystorage"

  before {
    EntitiesTableCreator.create(cassandraTableName, session)
    EntitiesTableCreator.createIndex(cassandraTableName, "saved", session)
    entityDao = new EntityDaoCassandraImpl(cassandraTableName, session)
  }

  // Fixture
  val tenantId = "TestTenantId"
  val created = DateTimeConverter.now
  val updated = created.plusDays(5)
  val dataObjectReference = testDataObjectReference
  val dataObjectReport = testDataObjectReport

  val entityInfos = List(
    testEntityInfo(tenantId, 3),
    testEntityInfo(tenantId, 14),
    testEntityInfo(tenantId + "otherTenant"),
    testEntityInfo(tenantId, 15).copy(saved = false))
  val entityCreates = List(
    CreateEntityRequest(entityInfos(0), Some(dataObjectReference), dataObjectReport),
    CreateEntityRequest(entityInfos(1), None, dataObjectReport),
    CreateEntityRequest(entityInfos(2), None, dataObjectReport),
    CreateEntityRequest(entityInfos(3), None, dataObjectReport))

  val inDb = entityInfos.zip(entityCreates)

  "Entities" should {
    "select all saved rows from a tenant" in withStoredEntities(inDb) {
      whenReady(entityDao.getAllSaved(tenantId)) { tenantEntities =>
        tenantEntities should contain theSameElementsAs Seq(entityInfos(0), entityInfos(1))
      }
    }
    "save and get an entity" that {
      val tenantId = "save_get_tenantId"
      "has a report" in {
        val entity = testEntityWithReport(tenantId, 0, dataObjectReport)
        val entityCreate = CreateEntityRequest(entity)
        whenReady(entityDao.create(entity.info.entityId, entityCreate, entity.info.created)) { _ =>
          whenReady(entityDao.getWithReport(tenantId, entity.info.entityId)) { getEntity =>
            getEntity shouldBe Some(entity)
          }
        }
      }
      "has data" in {
        val entity = testEntityWithData(tenantId, 0, dataObjectReference)
        val entityCreate = CreateEntityRequest(entity.info, Some(entity.dataReference), dataObjectReport)
        whenReady(entityDao.create(entity.info.entityId, entityCreate, entity.info.created)) { _ =>
          whenReady(entityDao.getWithData(tenantId, entity.info.entityId)) { getEntity =>
            getEntity shouldBe Some(entity)
          }
        }
      }
    }
    "update an entity" in withStoredEntities(inDb) {
      val (info, create) = inDb(1)
      val modifiedEntity = modifyEntity(UpdateEntityRequest(create))
      val now = DateTimeConverter.now
      whenReady(entityDao.update(info.tenantId, info.entityId, modifiedEntity, now)) { _ =>
        whenReady(entityDao.getWithReport(tenantId, info.entityId)) { getEntity =>
          UpdateEntityRequest(getEntity.get) shouldBe modifiedEntity
        }
      }
    }
    "delete an entity" in {
      val idToDelete = entityInfos(1).entityId
      whenReady(entityDao.delete(tenantId, idToDelete)) { _ =>
        whenReady(entityDao.getWithReport(tenantId, idToDelete)) { getEntity =>
          getEntity shouldBe None
        }
      }
    }
  }

  private def withStoredEntities(
      storedEntities: List[(EntityInfo, CreateEntityRequest)])(testCode: => Any): Unit = {
    val s = Future.sequence(storedEntities.map {
      case (info, entity) => entityDao.create(info.entityId, entity, info.created)
    })
    Await.ready(s, operationDuration)
    try {
      testCode
    } finally {
      session.execute(new QueryBuilder(session.getCluster).truncate(cassandraTableName))
    }
  }
}
