/**
 * Copyright 2015, deepsense.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.deepsense.entitystorage

import akka.actor.ActorSystem
import akka.testkit.{TestKit, TestProbe}
import org.scalatest._
import org.scalatest.concurrent.{Eventually, ScalaFutures}
import org.scalatest.mock.MockitoSugar

import io.deepsense.models.entities.{EntityWithData, CreateEntityRequest, Entity}
import io.deepsense.models.protocols.EntitiesApiActorProtocol.{Create, Get}

// FIXME Extract the traits into a single trait
// it's almost a complete copy-and-paste from EntitiesApiActorSpec
class EntityStorageClientSpec(actorSystem: ActorSystem)
  extends TestKit(actorSystem)
  with FlatSpecLike
  with Matchers
  with ScalaFutures
  with MockitoSugar
  with BeforeAndAfterAll
  with Eventually {

  val serviceProbe = TestProbe()
  val client = TestActorBasedEntityStorageClientFactory.create(serviceProbe.ref)

  def this() = this(ActorSystem("EntityStorageClientSpec"))

  "EntityStorage Client API" should "return Entity (using getEntityData)" in {

    // FIXME Generate the data using scalacheck
    val tenantId = "tenantId"
    val id = Entity.Id.randomId
    val entity = mock[EntityWithData]
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

    val entityToCreate = mock[CreateEntityRequest]
    val createdId = mock[Entity.Id]
    val entityF = client.createEntity(entityToCreate)

    serviceProbe.expectMsg(Create(entityToCreate))

    serviceProbe.reply(createdId)

    whenReady(entityF) {
      _ shouldBe createdId
    }
  }

  override def afterAll(): Unit = TestKit.shutdownActorSystem(system)
}
