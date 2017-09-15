/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 *  Owner: Rafal Hryciuk
 */

package io.deepsense.entitystorage.api.rest

import scala.concurrent.Future

import org.mockito.Matchers._
import org.mockito.Mockito._
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import org.scalatest.Matchers
import spray.http.StatusCodes
import spray.json.DefaultJsonProtocol
import spray.routing._

import io.deepsense.commons.auth.usercontext.{TokenTranslator, UserContext}
import io.deepsense.commons.auth.{Authorizator, AuthorizatorProvider}
import io.deepsense.commons.{StandardSpec, UnitTestSupport}
import io.deepsense.entitystorage.factories.EntityTestFactory
import io.deepsense.entitystorage.json.EntityJsonProtocol
import io.deepsense.entitystorage.models._
import io.deepsense.entitystorage.services.EntityService

class EntitiesApiSpec
  extends StandardSpec
  with UnitTestSupport
  with DefaultJsonProtocol
  with EntityJsonProtocol
  with Matchers
  with EntityTestFactory {

  val correctTenantA: String = "Mr Mojo Risin"

  val apiPrefix: String = "v1/entities"

  val entities = List(
    testEntity(correctTenantA, 1, Some(testDataObjectReference), Some(testDataObjectReport)),
    testEntity(correctTenantA, 2, Some(testDataObjectReference), Some(testDataObjectReport)))

  val addedEntity = entities.head
  val addedEntityDescriptor = UserEntityDescriptor(addedEntity)

  val notAddedEntity = testEntity(
    correctTenantA, 3, Some(testDataObjectReference), Some(testDataObjectReport))
  val notAddedEntityDescriptor = UserEntityDescriptor(notAddedEntity)

  val entityService = createMockEntityService

  "GET /entities" should {
    "return entities" in {
      Get(s"/$apiPrefix") ~>
        addHeader("X-Auth-Token", correctTenantA) ~> testRoute ~> check {
        status should be(StatusCodes.OK)

        implicit val entityProtocol = EntityJsonProtocol
        val expectedEntitiesMap: Map[String, Map[String, CompactEntityDescriptor]] =
          Map("entities" ->
            entities.map(e => e.id.value.toString -> CompactEntityDescriptor(e)).toMap)

        responseAs[Map[String, Map[String, CompactEntityDescriptor]]] shouldBe expectedEntitiesMap
      }
    }
  }

  "PUT /entities/:id" should {
    "return NotFound" when {
      "entity does not exists" in {
        Put(s"/$apiPrefix/${notAddedEntity.id}", notAddedEntityDescriptor) ~>
          addHeader("X-Auth-Token", correctTenantA) ~> testRoute ~> check {
          status should be(StatusCodes.NotFound)
        }
      }
    }
    "return BadRequest" when {
      "entity's Id from json does not match Id from request's URL" in {
        val entity1 :: entity2 :: _ = entities
        Put(s"/$apiPrefix/${entity1.id}", UserEntityDescriptor(entity2)) ~>
          addHeader("X-Auth-Token", correctTenantA) ~> testRoute ~> check {
          status should be(StatusCodes.BadRequest)
        }
      }
    }
    "update entity and return it's report" in {
      Put(s"/$apiPrefix/${addedEntityDescriptor.id}", addedEntityDescriptor) ~>
        addHeader("X-Auth-Token", correctTenantA) ~> testRoute ~> check {
        status should be(StatusCodes.OK)
        responseAs[Entity] shouldBe entities.head
        verify(entityService).updateEntity(correctTenantA, addedEntityDescriptor)
      }
    }
  }

  "DELETE /entities/:id" should {
    "return Status OK and delete entity" in {
      Delete(s"/$apiPrefix/${addedEntityDescriptor.id}") ~>
        addHeader("X-Auth-Token", correctTenantA) ~> testRoute ~> check {
        status should be(StatusCodes.OK)
        verify(entityService).deleteEntity(correctTenantA, addedEntityDescriptor.id)
      }
    }
  }

  protected def testRoute = {
    val tokenTranslator = mock[TokenTranslator]
    when(tokenTranslator.translate(any(classOf[String])))
      .thenAnswer(new Answer[Future[UserContext]] {
      override def answer(invocation: InvocationOnMock): Future[UserContext] = {
        Future.successful(createMockUserContext)
      }
    })
    createRestComponent(tokenTranslator)
  }

  private def createMockUserContext: UserContext = {
    val context = mock[UserContext]
    when(context.tenantId).thenReturn(correctTenantA)
    context
  }

  private def createMockEntityService: EntityService = {
    val entityService = mock[EntityService]
    when(entityService.getAll(anyString())).thenReturn(Future.successful(entities))
    when(entityService.deleteEntity(any(), any())).thenReturn(Future.successful(()))
    when(entityService.updateEntity(anyString(), any())).thenAnswer(
      new Answer[Future[Option[Entity]]] {
        override def answer(invocationOnMock: InvocationOnMock): Future[Option[Entity]] = {
          val entity = invocationOnMock.getArgumentAt(1, classOf[UserEntityDescriptor])
          val result = if (entity == notAddedEntityDescriptor) None else Some(addedEntity)
          Future.successful(result)
        }
      })
    entityService
  }

  private def createRestComponent(tokenTranslator: TokenTranslator): Route  =  new EntitiesApi(
    tokenTranslator,
    entityService,
    new AllAllowedAuthorizationProvider(),
    apiPrefix, "role1", "role2", "role3").route

  private class AllAllowedAuthorizationProvider extends AuthorizatorProvider {
    override def forContext(userContext: Future[UserContext]): Authorizator = {
      new Authorizator {
        override def withRole[T](role: String)(onSuccess: UserContext => Future[T]): Future[T] =
          userContext.flatMap(onSuccess)
      }
    }
  }
}
