/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.entitystorage

import scala.concurrent.duration._

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfter, Matchers}

import io.deepsense.commons.StandardSpec
import io.deepsense.commons.rest.RestServer
import io.deepsense.entitystorage.factories.EntityTestFactory
import io.deepsense.entitystorage.storage.EntityDao
import io.deepsense.models.entities.Entity

class EntityStorageClientIntegSpec
  extends StandardSpec
  with ScalaFutures
  with Matchers
  with BeforeAndAfter
  with CassandraTestSupport
  with EntityTestFactory
  with EntityStorageIntegTestSupport {

  var clientFactory: EntityStorageClientFactory = _
  var client: EntityStorageClient = _
  val entityDao = getInstance[EntityDao]
  getInstance[RestServer].start()

  implicit val callDuration = 5.seconds

  before {
    EntitiesTableCreator.create(table, session)
    clientFactory = EntityStorageClientFactoryImpl()
    client = clientFactory.create("root-actor-system", "127.0.0.1", 2552, "EntitiesApiActor", 10)
  }

  after {
    clientFactory.close()
  }

  "EntityStorageClient.createEntity(...)" should {
    "create an entity" in {
      val inputEntity = testInputEntity
      val eventualEntity = client.createEntity(inputEntity)
      whenReady(eventualEntity) { entity =>
        whenReady(entityDao.get(inputEntity.tenantId, entity.id)) { maybeEntity =>
          maybeEntity.get should have(
            'tenantId (inputEntity.tenantId),
            'name (inputEntity.name),
            'description (inputEntity.description),
            'dClass (inputEntity.dClass),
            'data (inputEntity.data),
            'report (inputEntity.report),
            'saved (inputEntity.saved)
          )
        }
      }
    }
  }
  "EntityStorageClient.getEntityData()" should {
    "return an entity if it exists" in {
      val existingEntity = testEntity
      whenReady(entityDao.upsert(existingEntity)) { _ =>
        whenReady(client.getEntityData(
          existingEntity.tenantId,
          existingEntity.id)) { maybeEntity =>
          maybeEntity.get shouldBe existingEntity.dataOnly
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
