/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Jacek Laskowski
 */
package io.deepsense.entitystorage

import akka.actor.ActorSystem
import akka.testkit.{TestKit, TestProbe}
import org.scalatest._
import org.scalatest.concurrent.{Eventually, ScalaFutures}
import org.scalatest.mock.MockitoSugar

import io.deepsense.entitystorage.api.akka.EntitiesApiActor.{Create, Get}
import io.deepsense.entitystorage.factories.EntityTestFactory
import io.deepsense.models.entities.Entity

// FIXME Extract the traits into a single trait
// it's almost a complete copy-and-paste from EntitiesApiActorSpec
class EntityStorageClientSpec(actorSystem: ActorSystem)
  extends TestKit(actorSystem)
  with FlatSpecLike
  with Matchers
  with ScalaFutures
  with MockitoSugar
  with BeforeAndAfterAll
  with EntityTestFactory
  with Eventually {

  val serviceProbe = TestProbe()
  val returnTestEntity = testEntity
  val client = TestEntityStorageClientFactory.create(serviceProbe.ref)

  def this() = this(ActorSystem("EntityStorageClientSpec"))

  "EntityStorage Client API" should "return Entity (using getEntityData)" in {

    // FIXME Generate the data using scalacheck
    val tenantId = "tenantId"
    val id = Entity.Id.randomId
    val entity = mock[Entity]
    val returnEntity = Some(entity)

    import scala.concurrent.duration._
    implicit val timeout = 5.seconds

    val entityF = client.getEntityData(tenantId, id)

    serviceProbe.expectMsg(Get(tenantId, id))
    serviceProbe.reply(returnEntity)

    whenReady(entityF) {
      _ shouldBe returnEntity
    }
  }

  it should "create Entity (using createEntity)" in {

    import scala.concurrent.duration._
    implicit val timeout = 5.seconds

    val entityToCreate = testInputEntity
    val entityF = client.createEntity(entityToCreate)

    serviceProbe.expectMsg(Create(entityToCreate))
    serviceProbe.reply(returnTestEntity)

    whenReady(entityF) {
      _ shouldBe returnTestEntity
    }
  }

  override def afterAll() {
    TestKit.shutdownActorSystem(system)
  }
}
