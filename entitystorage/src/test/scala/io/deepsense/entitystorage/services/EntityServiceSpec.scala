/**
 * Copyright (c) 2015, CodiLime, Inc.
 */

package io.deepsense.entitystorage.services

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import org.joda.time.{DateTime, DateTimeComparator}
import org.mockito.Matchers.any
import org.mockito.Mockito.{verify, when}
import org.mockito.{ArgumentCaptor, Mockito}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mock.MockitoSugar
import org.scalatest.{BeforeAndAfter, FlatSpec, Matchers}

import io.deepsense.entitystorage.factories.EntityTestFactory
import io.deepsense.entitystorage.models.EntityMatchers
import io.deepsense.entitystorage.storage.EntityDao
import io.deepsense.models.entities.{UserEntityDescriptor, Entity}

class EntityServiceSpec
  extends FlatSpec
  with Matchers
  with EntityMatchers
  with MockitoSugar
  with EntityTestFactory
  with BeforeAndAfter
  with ScalaFutures {

  val entity = testEntity
  val entities = List(testEntity)
  val nonExistingEntityId = Entity.Id.randomId
  val entityDao: EntityDao = mock[EntityDao]
  val entityService: EntityService = new EntityService(entityDao)

  before {
    Mockito.reset(entityDao)
    when(entityDao.get(entity.tenantId, entity.id)).thenReturn(Future.successful(Some(entity)))
    when(entityDao.get(entity.tenantId, nonExistingEntityId)).thenReturn(Future.successful(None))
    when(entityDao.getAll(entity.tenantId)).thenReturn(Future.successful(entities))
    when(entityDao.upsert(any())).thenReturn(Future.successful(()))
    when(entityDao.delete(any(), any())).thenReturn(Future.successful(()))
  }

  "EntityService" should "set id, update time and create time when create entity" in {
    entityService.createEntity(testInputEntity)

    val captor: ArgumentCaptor[Entity] = ArgumentCaptor.forClass(classOf[Entity])
    verify(entityDao).upsert(captor.capture())
    val entity = captor.getValue
    entity.id should not be null
    entity.created should not be null
    entity.updated should not be null
  }

  it should "set all fields from inputEntity when create entity" in {
    val inputEntity = testInputEntity

    entityService.createEntity(inputEntity)

    val captor: ArgumentCaptor[Entity] = ArgumentCaptor.forClass(classOf[Entity])
    verify(entityDao).upsert(captor.capture())
    val entity = captor.getValue

    entity should have (
      tenantId(inputEntity.tenantId),
      dClass(inputEntity.dClass),
      name(inputEntity.name),
      description(inputEntity.description),
      saved(inputEntity.saved)
    )
    entity.data shouldBe inputEntity.data
    entity.report shouldBe inputEntity.report
  }

  it should "return only report when asked for entity report" in {
    whenReady(entityService.getEntityReport(entity.tenantId, entity.id)) { retrievedEntity =>
      retrievedEntity.get.data shouldBe None
      retrievedEntity.get.report shouldBe entity.report
    }
  }

  it should "return None when asked for non-existing entity report" in {
    whenReady(entityService.getEntityReport(entity.tenantId, nonExistingEntityId)) {
      retrievedEntity => retrievedEntity shouldBe None
    }
  }

  it should "return only data when asked for entity data" in {
    whenReady(entityService.getEntityData(entity.tenantId, entity.id)) { retrievedEntity =>
      retrievedEntity.get.data shouldBe entity.data
      retrievedEntity.get.report shouldBe None
    }
  }

  it should "return None when asked for non-existing entity data" in {
    whenReady(entityService.getEntityData(entity.tenantId, nonExistingEntityId)) {
      retrievedEntity => retrievedEntity shouldBe None
    }
  }

  it should "update entity and return only report" in {

    implicit object DateTimeOrdering extends Ordering[org.joda.time.DateTime] {
      val dtComparator = DateTimeComparator.getInstance()

      def compare(x: DateTime, y: DateTime): Int = {
        dtComparator.compare(x, y)
      }
    }

    val updatedEntity = UserEntityDescriptor(entity).copy(
      name = "new name", description = "new description", saved = false)

    whenReady(entityService.updateEntity(entity.tenantId, updatedEntity)) { retrievedEntityOption =>
      val retrievedEntity = retrievedEntityOption.get
      retrievedEntity.updated should be > entity.updated

      val expectedEntity = entity.copy(
        name = updatedEntity.name,
        description = updatedEntity.description,
        saved = updatedEntity.saved,
        updated = retrievedEntity.updated)
      retrievedEntity shouldBe expectedEntity.reportOnly

      verify(entityDao).upsert(expectedEntity)
    }
  }

  it should "return None when asked for updating non-existing entity" in {
    val updatedEntity = UserEntityDescriptor(entity).copy(id = nonExistingEntityId)
    whenReady(entityService.updateEntity(entity.tenantId, updatedEntity)) { retrievedEntity =>
      retrievedEntity shouldBe None
    }
  }

  it should "return all entities of tenant from entityDao" in {
    whenReady(entityService.getAll(entity.tenantId)) { retrievedEntities =>
      retrievedEntities shouldBe entities
    }
  }

  it should "delete entity" in {
    whenReady(entityService.deleteEntity(entity.tenantId, entity.id)) { _ =>
      verify(entityDao).delete(entity.tenantId, entity.id)
    }
  }
}
