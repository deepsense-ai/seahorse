/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 *  Owner: Rafal Hryciuk
 */

package io.deepsense.entitystorage.api.rest

import scala.concurrent.Future

import org.joda.time.DateTime
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
import io.deepsense.commons.datetime.DateTimeConverter
import io.deepsense.commons.{StandardSpec, UnitTestSupport}
import io.deepsense.deeplang.doperables.Report
import io.deepsense.entitystorage.json.EntityJsonProtocol
import io.deepsense.entitystorage.models._
import io.deepsense.entitystorage.storage.EntityDao

class EntitiesApiSpec
  extends StandardSpec
  with UnitTestSupport
  with DefaultJsonProtocol
  with EntityJsonProtocol
  with Matchers {

  val correctTenant: String = "A"
  val apiPrefix: String = "v1/entities"
  val entities = List(
    createEntity(
      1,
      DateTimeConverter.now,
      DateTimeConverter.now.plusHours(1),
      DataObjectReference("hdfs://whatever")),
    createEntity(
      2,
      DateTimeConverter.now.plusDays(1),
      DateTimeConverter.now.plusDays(2),
      DataObjectReport(Report()))
  )
  val expectedEntitiesMap: Map[String, Map[String, EntityDescriptor]] =
    Map("entities" -> entities.map(e => e.id.value.toString -> EntityDescriptor(e)).toMap)


  "GET /entities" should {
    "return entities" in {
      Get(s"/$apiPrefix") ~>
        addHeader("X-Auth-Token", correctTenant) ~> testRoute ~> check {
        status should be(StatusCodes.OK)
        implicit val entityProtocol = EntityJsonProtocol
        responseAs[Map[String, Map[String, EntityDescriptor]]] shouldBe expectedEntitiesMap
      }
    }
  }

  private def createEntity(
      index: Int,
      created: DateTime,
      updated: DateTime,
      data: DataObject): Entity = Entity(
    Entity.Id.randomId,
    s"name$index",
    s"description$index",
    s"dClass$index",
    created,
    updated,
    data
  )

  private def testRoute = {
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
