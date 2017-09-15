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
import io.deepsense.entitystorage.api.akka.EntitiesApiActor.Get
import io.deepsense.entitystorage.factories.EntityTestFactory
import io.deepsense.entitystorage.models.{DataObjectReference, Entity}
import io.deepsense.entitystorage.storage.EntityDao

class EntitiesApiActorSpec
  extends FlatSpec
  with MockitoSugar
  with EntityTestFactory
  with Matchers
  with ScalaFutures
  with BeforeAndAfterAll {

  implicit var system: ActorSystem = _
  var actorRef: TestActorRef[EntitiesApiActor] = _
  var testProbe: TestProbe = _

  implicit val timeout: Timeout = Timeout(1 second)
  val tenantId = "tenantId"
  val existingEntityId = Entity.Id.randomId
  val notExistingEntityId = Entity.Id.randomId
  val entity = testEntity("DataFrame", DataObjectReference("some url"))

  "EntityApiActor" should "send entity if exists" in {
    testProbe.send(actorRef, Get(tenantId, existingEntityId))

    testProbe.expectMsgType[Option[Entity]] shouldBe Some(entity)
  }

  it should "send None when entity does not exist" in {
    testProbe.send(actorRef, Get(tenantId, notExistingEntityId))

    testProbe.expectMsgType[Option[Entity]] shouldBe None
  }

  override def beforeAll(): Unit = {
    system = ActorSystem("test")
    actorRef = TestActorRef(new EntitiesApiActor(entityDao))
    testProbe = TestProbe()
  }

  override def afterAll(): Unit = {
    system.shutdown()
  }

  private def entityDao: EntityDao = {
    val entityDao: EntityDao = mock[EntityDao]
    when(entityDao.get(org.mockito.Matchers.eq(tenantId), isA(classOf[Id])))
      .thenReturn(Future.successful(None))
    when(entityDao.get(tenantId, existingEntityId)).thenReturn(Future.successful(Some(entity)))
    entityDao
  }
}
