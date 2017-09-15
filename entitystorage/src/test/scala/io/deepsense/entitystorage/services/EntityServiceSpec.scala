/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.entitystorage.services

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import org.joda.time.{DateTime, DateTimeComparator}
import org.mockito.Matchers.{any, isNotNull, eq => mockEq}
import org.mockito.Mockito.{verify, when}
import org.mockito.{ArgumentCaptor, Mockito}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mock.MockitoSugar
import org.scalatest.{BeforeAndAfter, FlatSpec, Matchers}

import io.deepsense.entitystorage.factories.EntityTestFactory
import io.deepsense.entitystorage.storage.EntityDao
import io.deepsense.models.entities.{Entity, UpdateEntityRequest}

class EntityServiceSpec
  extends FlatSpec
  with Matchers
  with MockitoSugar
  with EntityTestFactory
  with BeforeAndAfter
  with ScalaFutures {

  val entity = testEntityWithReport()
  val entityWithReport = testEntityWithReport()
  val entityWithData = testEntityWithData()
  val entities = List(entity, entityWithReport, entityWithData)
  val nonExistingEntityId = Entity.Id.randomId
  val entityDao: EntityDao = mock[EntityDao]
  val entityService: EntityService = new EntityService(entityDao)

  before {
    Mockito.reset(entityDao)

    when(entityDao.getWithReport(entityWithReport.info.tenantId, entityWithReport.info.entityId))
      .thenReturn(Future.successful(Some(entityWithReport)))
    when(entityDao.getWithReport(entityWithReport.info.tenantId, nonExistingEntityId))
      .thenReturn(Future.successful(None))

    when(entityDao.getWithData(entityWithData.info.tenantId, entityWithData.info.entityId))
      .thenReturn(Future.successful(Some(entityWithData)))
    when(entityDao.getWithData(entityWithData.info.tenantId, nonExistingEntityId))
      .thenReturn(Future.successful(None))

    when(entityDao.getAll(entityWithReport.info.tenantId))
      .thenReturn(Future.successful(entities.map(_.info)))

    when(entityDao.create(any(), any(), any())).thenReturn(Future.successful(()))
    when(entityDao.update(any(), any(), any(), any())).thenReturn(Future.successful(()))

    when(entityDao.delete(any(), any())).thenReturn(Future.successful(()))
  }

  "EntityService" should "set id and create time and create entity using Dao" in {
    val createdEntity = testCreateEntityRequest()
    whenReady(entityService.createEntity(createdEntity)) { id =>
      verify(entityDao).create(mockEq(id), mockEq(createdEntity), isNotNull(classOf[DateTime]))
    }
    ()
  }

  it should "return entity with report" in {
    whenReady(entityService.getEntityReport(
        entityWithReport.info.tenantId, entityWithReport.info.entityId)) {
      retrievedEntity => retrievedEntity shouldBe Some(entityWithReport)
    }
  }

  it should "return None when asked for non-existing entity report" in {
    whenReady(entityService.getEntityReport(
        entityWithReport.info.tenantId, nonExistingEntityId)) {
      retrievedEntity => retrievedEntity shouldBe None
    }
  }

  it should "return entity with data" in {
    whenReady(entityService.getEntityData(
        entityWithData.info.tenantId, entityWithData.info.entityId)) {
      retrievedEntity => retrievedEntity shouldBe Some(entityWithData)
    }
  }

  it should "return None when asked for non-existing entity data" in {
    whenReady(entityService.getEntityData(entityWithData.info.tenantId, nonExistingEntityId)) {
      retrievedEntity => retrievedEntity shouldBe None
    }
  }

  it should "update entity and return entity with report" in {

    implicit object DateTimeOrdering extends Ordering[org.joda.time.DateTime] {
      val dtComparator = DateTimeComparator.getInstance()

      def compare(x: DateTime, y: DateTime): Int = {
        dtComparator.compare(x, y)
      }
    }

    val entityToUpdate = UpdateEntityRequest(entityWithReport)
    val updatedEntity = modifyEntity(entityToUpdate)

    val updatedCaptor = ArgumentCaptor.forClass(classOf[DateTime])

    whenReady(entityService.updateEntity(
        entityWithReport.info.tenantId, entityWithReport.info.entityId, updatedEntity)) {
      retrievedEntityOption =>
        val retrievedEntity = retrievedEntityOption.get
        retrievedEntity shouldBe entityWithReport
        // that's what's returned by dao on getEntityWithReport

        verify(entityDao).update(
          mockEq(entityWithReport.info.tenantId),
          mockEq(entityWithReport.info.entityId),
          mockEq(updatedEntity),
          updatedCaptor.capture())

        updatedCaptor.getValue should be > entity.info.updated
    }
    ()
  }

  it should "return None when asked for updating non-existing entity" in {
    val updatedEntity = UpdateEntityRequest(entityWithReport)
    whenReady(entityService.updateEntity(
        entity.info.tenantId, nonExistingEntityId, updatedEntity)) {
      retrievedEntity => retrievedEntity shouldBe None
    }
  }

  it should "return all entities of tenant from entityDao" in {
    whenReady(entityService.getAll(entity.info.tenantId)) { retrievedEntities =>
      retrievedEntities shouldBe entities.map(_.info)
    }
  }

  it should "delete entity" in {
    whenReady(entityService.deleteEntity(entity.info.tenantId, entity.info.entityId)) { _ =>
        verify(entityDao).delete(entity.info.tenantId, entity.info.entityId)
      ()
    }
  }
}
