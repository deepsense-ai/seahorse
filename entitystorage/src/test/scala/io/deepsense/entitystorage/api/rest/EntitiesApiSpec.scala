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
import io.deepsense.entitystorage.storage.EntityDao

class EntitiesApiSpec
  extends StandardSpec
  with UnitTestSupport
  with DefaultJsonProtocol
  with EntityJsonProtocol
  with Matchers
  with EntityTestFactory {

  val correctTenant: String = "A"
  val apiPrefix: String = "v1/entities"
  val tenantId = "Mr Mojo Risin"
  val entities = List(
    testEntity(tenantId, 1, Some(testDataObjectReference), Some(testDataObjectReport)),
    testEntity(tenantId, 2, Some(testDataObjectReference), Some(testDataObjectReport)))
  val expectedEntitiesMap: Map[String, Map[String, CompactEntityDescriptor]] =
    Map("entities" -> entities.map(e => e.id.value.toString -> CompactEntityDescriptor(e)).toMap)


  "GET /entities" should {
    "return entities" in {
      Get(s"/$apiPrefix") ~>
        addHeader("X-Auth-Token", correctTenant) ~> testRoute ~> check {
        status should be(StatusCodes.OK)
        implicit val entityProtocol = EntityJsonProtocol
        responseAs[Map[String, Map[String, CompactEntityDescriptor]]] shouldBe expectedEntitiesMap
      }
    }
  }

  protected def testRoute = {
    val tokenTranslator = mock[TokenTranslator]
    when(tokenTranslator.translate(any(classOf[String])))
      .thenAnswer(new Answer[Future[UserContext]] {
      override def answer(invocation: InvocationOnMock): Future[UserContext] = {
        Future.successful(mockUserContext)
      }
    })
    createRestComponent(tokenTranslator)
  }

  private def mockUserContext: UserContext = mock[UserContext]

  private def entityDao: EntityDao = {
    val entityDao = mock[EntityDao]
    when(entityDao.getAll(anyString())).thenReturn(Future.successful(entities))
    entityDao
  }

  private def createRestComponent(tokenTranslator: TokenTranslator): Route  =  new EntitiesApi(
    tokenTranslator,
    entityDao,
    new AllAlowedAuthorizationProvider(),
    apiPrefix, "role").route


  private class AllAlowedAuthorizationProvider extends AuthorizatorProvider {
    override def forContext(userContext: Future[UserContext]): Authorizator = {
      new Authorizator {
        override def withRole[T](role: String)(onSuccess: UserContext => Future[T]): Future[T] =
          userContext.flatMap(onSuccess)
      }
    }
  }
}
