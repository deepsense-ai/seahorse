/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 *  Owner: Rafal Hryciuk
 */

package io.deepsense.entitystorage.api.akka

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps

import akka.actor.ActorSystem
import akka.testkit.{TestActorRef, TestProbe}
import akka.util.Timeout
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mock.MockitoSugar
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}

import io.deepsense.commons.models.Id
import io.deepsense.entitystorage.api.akka.EntitiesApiActor.{Create, Get}
import io.deepsense.entitystorage.factories.EntityTestFactory
import io.deepsense.entitystorage.models.Entity
import io.deepsense.entitystorage.services.EntityService

class EntitiesApiActorSpec
  extends FlatSpec
  with MockitoSugar
  with EntityTestFactory
  with Matchers
  with ScalaFutures
  with BeforeAndAfterAll {

  implicit var system: ActorSystem = _
  var entityService: EntityService = _
  var actorRef: TestActorRef[EntitiesApiActor] = _
  var testProbe: TestProbe = _

  implicit val timeout: Timeout = Timeout(1 second)
  val tenantId = "tenantId"
  val notExistingEntityId = Entity.Id.randomId
  val entity = testEntity
  val existingEntityId = entity.id

  "EntityApiActor" should "send entity if exists" in {
    testProbe.send(actorRef, Get(tenantId, existingEntityId))

    testProbe.expectMsgType[Option[Entity]] shouldBe Some(entity.dataOnly)
  }

  it should "send None when entity does not exist" in {
    testProbe.send(actorRef, Get(tenantId, notExistingEntityId))

    testProbe.expectMsgType[Option[Entity]] shouldBe None
  }

  it should "create Entity in the storage" in {
    val inputEntity = testInputEntity
    testProbe.send(actorRef, Create(inputEntity))

    verify(entityService, times(1)).createEntity(inputEntity)
  }

  override def beforeAll(): Unit = {
    system = ActorSystem("test")
    entityService = createEntityService
    actorRef = TestActorRef(new EntitiesApiActor(entityService))
    testProbe = TestProbe()
  }

  override def afterAll(): Unit = {
    system.shutdown()
  }

  private def createEntityService: EntityService = {
    val entityService: EntityService = mock[EntityService]
    when(entityService.getEntityData(org.mockito.Matchers.eq(tenantId), isA(classOf[Id])))
      .thenReturn(Future.successful(None))
    when(entityService.getEntityData(tenantId, existingEntityId))
      .thenReturn(Future.successful(Some(entity.dataOnly)))
    entityService
  }
}
