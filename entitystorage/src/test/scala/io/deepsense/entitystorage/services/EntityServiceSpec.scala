/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 *  Owner: Rafal Hryciuk
 */

package io.deepsense.entitystorage.services

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import org.mockito.Mockito.{verify, when}
import org.mockito.{ArgumentCaptor, Mockito}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mock.MockitoSugar
import org.scalatest.{BeforeAndAfter, FlatSpec, Matchers}

import io.deepsense.entitystorage.factories.EntityTestFactory
import io.deepsense.entitystorage.models.{Entity, EntityMatchers}
import io.deepsense.entitystorage.storage.EntityDao

class EntityServiceSpec
  extends FlatSpec
  with Matchers
  with EntityMatchers
  with MockitoSugar
  with EntityTestFactory
  with BeforeAndAfter
  with ScalaFutures {

  val entity = testEntity
  val entityDao: EntityDao = mock[EntityDao]
  val entityService: EntityService = new EntityService(entityDao)

  before {
    Mockito.reset(entityDao)
    when(entityDao.get(entity.tenantId, entity.id)).thenReturn(Future.successful(Some(entity)))
  }

  "EntityService" should "set id, update time and create time when create entity" in {
    val inputEntity = testInputEntity

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

    entityService.createEntity(testInputEntity)

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

  it should "return only data when asked for entity data" in {
    whenReady(entityService.getEntityData(entity.tenantId, entity.id)) { retrievedEntity =>
      retrievedEntity.get.data shouldBe entity.data
      retrievedEntity.get.report shouldBe None
    }
  }
}
