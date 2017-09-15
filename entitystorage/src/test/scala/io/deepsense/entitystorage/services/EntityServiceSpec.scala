/**
 * Copyright (c) 2015, CodiLime Inc.
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
import io.deepsense.entitystorage.storage.EntityDao
import io.deepsense.models.entities.{EntityInfo, EntityCreate, Entity, EntityUpdate}

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

    when(entityDao.getWithReport(entityWithReport.info.tenantId, entityWithReport.info.id))
      .thenReturn(Future.successful(Some(entityWithReport)))
    when(entityDao.getWithReport(entityWithReport.info.tenantId, nonExistingEntityId))
      .thenReturn(Future.successful(None))

    when(entityDao.getWithData(entityWithData.info.tenantId, entityWithData.info.id))
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
    val createdEntity = testEntityCreate()
    whenReady(entityService.createEntity(createdEntity)) { id =>
      val idCaptor = ArgumentCaptor.forClass(classOf[Entity.Id])
      val entityCaptor = ArgumentCaptor.forClass(classOf[EntityCreate])
      val createdCaptor = ArgumentCaptor.forClass(classOf[DateTime])
      verify(entityDao).create(idCaptor.capture(), entityCaptor.capture(), createdCaptor.capture())
      idCaptor.getValue shouldBe id
      createdCaptor.getValue should not be null
      entityCaptor.getValue shouldBe createdEntity
    }
  }

  it should "return entity with report" in {
    whenReady(entityService.getEntityReport(
        entityWithReport.info.tenantId, entityWithReport.info.id)) {
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
        entityWithData.info.tenantId, entityWithData.info.id)) {
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

    val entityToUpdate = EntityUpdate(entityWithReport)
    val updatedEntity = modifyEntity(entityToUpdate)

    val tenantIdCaptor = ArgumentCaptor.forClass(classOf[String])
    val idCaptor = ArgumentCaptor.forClass(classOf[Entity.Id])
    val entityCaptor = ArgumentCaptor.forClass(classOf[EntityUpdate])
    val updatedCaptor = ArgumentCaptor.forClass(classOf[DateTime])

    whenReady(entityService.updateEntity(
        entityWithReport.info.tenantId, entityWithReport.info.id, updatedEntity)) {
      retrievedEntityOption =>
        val retrievedEntity = retrievedEntityOption.get
        retrievedEntity shouldBe entityWithReport
        // that's what returned by dao on getEntityWithReport

        verify(entityDao).update(
          tenantIdCaptor.capture(), idCaptor.capture(),
          entityCaptor.capture(), updatedCaptor.capture())

        tenantIdCaptor.getValue shouldBe entityWithReport.info.tenantId
        idCaptor.getValue shouldBe entityWithReport.info.id
        entityCaptor.getValue shouldBe updatedEntity
        updatedCaptor.getValue should be > entity.info.updated
    }
    ()
  }

  it should "return None when asked for updating non-existing entity" in {
    val updatedEntity = EntityUpdate(entityWithReport)
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
    whenReady(entityService.deleteEntity(entity.info.tenantId, entity.info.id)) { _ =>
        verify(entityDao).delete(entity.info.tenantId, entity.info.id)
      ()
    }
  }
}
