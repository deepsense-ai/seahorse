/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.entitystorage

import scala.concurrent.duration._

import org.joda.time.DateTime
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfter, Matchers}

import io.deepsense.commons.StandardSpec
import io.deepsense.commons.datetime.DateTimeConverter
import io.deepsense.commons.rest.RestServer
import io.deepsense.commons.cassandra.CassandraTestSupport
import io.deepsense.entitystorage.factories.EntityTestFactory
import io.deepsense.entitystorage.storage.EntityDao
import io.deepsense.models.entities.{EntityInfo, Entity}

class EntityStorageClientIntegSpec
  extends StandardSpec
  with ScalaFutures
  with Matchers
  with BeforeAndAfter
  with CassandraTestSupport
  with EntityTestFactory
  with EntityStorageIntegTestSupport {

  def cassandraTableName : String = "entities"
  def cassandraKeySpaceName : String = "entitystorage"

  var clientFactory: EntityStorageClientFactory = _
  var client: EntityStorageClient = _
  val entityDao = getInstance[EntityDao]
  getInstance[RestServer].start()

  implicit val callDuration = 5.seconds

  before {
    EntitiesTableCreator.create(cassandraTableName, session)
    clientFactory = EntityStorageClientFactoryImpl()
    client = clientFactory.create("root-actor-system", "127.0.0.1", 2552, "EntitiesApiActor", 10)
  }

  after {
    clientFactory.close()
  }

  "EntityStorageClient.createEntity(...)" should {
    "create an entity" in {
      val inputEntity = testCreateEntityRequest()
      val eventualEntityId = client.createEntity(inputEntity)
      whenReady(eventualEntityId) { entityId =>
        whenReady(entityDao.getWithReport(inputEntity.tenantId, entityId)) { maybeEntity =>
          val entity = maybeEntity.get
          entity.info should have(
            'entityId (entityId),
            'tenantId (inputEntity.tenantId),
            'name (inputEntity.name),
            'description (inputEntity.description),
            'dClass (inputEntity.dClass),
            'saved (inputEntity.saved)
          )
          entity.info.created shouldBe entity.info.updated
          entity.report shouldBe inputEntity.report
        }
        whenReady(entityDao.getWithData(inputEntity.tenantId, entityId)) { maybeEntity =>
          val entity = maybeEntity.get
          entity.dataReference shouldBe inputEntity.dataReference.get
        }
      }
      ()
    }
  }
  "EntityStorageClient.getEntityData()" should {
    "return an entity if it exists" in {
      val existingEntity = testCreateEntityRequest()
      val id = Entity.Id.randomId
      val created = DateTimeConverter.now
      whenReady(entityDao.create(id, existingEntity, created)) { _ =>
        whenReady(client.getEntityData(
          existingEntity.tenantId, id)) { maybeEntity =>
            val entity = maybeEntity.get
            entity.info shouldBe EntityInfo(existingEntity, id, created, created)
            entity.dataReference shouldBe existingEntity.dataReference.get
        }
      }
    }
    "return None if it entity does not exist" in {
      whenReady(client.getEntityData(
        "a non-existing tenant id",
        Entity.Id.randomId)) { _ shouldBe None }
    }
  }
}
