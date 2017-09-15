/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.entitystorage.api.rest

import scala.concurrent.Future

import org.mockito.Matchers._
import org.mockito.Mockito._
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import org.scalatest.Matchers
import spray.http._
import spray.json.DefaultJsonProtocol
import spray.routing._

import io.deepsense.commons.auth.usercontext.{TokenTranslator, UserContext}
import io.deepsense.commons.auth.{Authorizator, AuthorizatorProvider}
import io.deepsense.commons.{StandardSpec, UnitTestSupport}
import io.deepsense.entitystorage.factories.EntityTestFactory
import io.deepsense.entitystorage.json.EntityJsonProtocol
import io.deepsense.entitystorage.services.{EntityService, FileUploadService}
import io.deepsense.models.entities._

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
    testEntityWithReport(correctTenantA, 1),
    testEntityWithReport(correctTenantA, 2))

  val addedEntity = entities.head
  val addedEntityUpdate = UpdateEntityRequest(addedEntity)

  val notAddedEntity = testEntityWithReport(correctTenantA, 3)
  val notAddedEntityUpdate = UpdateEntityRequest(notAddedEntity)

  val entityService = createMockEntityService
  val fileUploadService = createMockFileUploadService

  "GET /entities" should {
    "return entities" in {
      Get(s"/$apiPrefix") ~>
        addHeader("X-Auth-Token", correctTenantA) ~> testRoute ~> check {
        status should be(StatusCodes.OK)

        implicit val entityProtocol = EntityJsonProtocol
        responseAs[Map[String, List[EntityInfo]]] shouldBe Map("entities" -> entities.map(_.info))
      }
      ()
    }
  }

  "GET /entities/:id/report" should {
    "return entities" in {
      Get(s"/$apiPrefix/${addedEntity.info.entityId}/report") ~>
        addHeader("X-Auth-Token", correctTenantA) ~> testRoute ~> check {
        status should be(StatusCodes.OK)

        implicit val entityProtocol = EntityJsonProtocol
        val expectedEntity = Map("entity" -> addedEntity)

        responseAs[Map[String, EntityWithReport]] shouldBe expectedEntity
      }
      ()
    }
    "return NotFound" when {
      "entity does not exists" in {
        Get(s"/$apiPrefix/${notAddedEntity.info.entityId}/report") ~>
          addHeader("X-Auth-Token", correctTenantA) ~> testRoute ~> check {
          status should be(StatusCodes.NotFound)
        }
        ()
      }
    }
  }

  "PUT /entities/:id" should {
    "return NotFound" when {
      "entity does not exists" in {
        Put(s"/$apiPrefix/${notAddedEntity.info.entityId}", notAddedEntityUpdate) ~>
          addHeader("X-Auth-Token", correctTenantA) ~> testRoute ~> check {
          status should be(StatusCodes.NotFound)
        }
        ()
      }
    }
    "update entity and return it's report" in {
      Put(s"/$apiPrefix/${addedEntity.info.entityId}", addedEntityUpdate) ~>
        addHeader("X-Auth-Token", correctTenantA) ~> testRoute ~> check {
        status should be(StatusCodes.OK)
        responseAs[Map[String, EntityWithReport]] shouldBe Map("entity" -> addedEntity)
        verify(entityService).updateEntity(
          correctTenantA, addedEntity.info.entityId, addedEntityUpdate)
      }
      ()
    }
  }

  "DELETE /entities/:id" should {
    "return Status OK and delete entity" in {
      Delete(s"/$apiPrefix/${addedEntity.info.entityId}") ~>
        addHeader("X-Auth-Token", correctTenantA) ~> testRoute ~> check {
        status should be(StatusCodes.OK)
        verify(entityService).deleteEntity(correctTenantA, addedEntity.info.entityId)
      }
      ()
    }
  }

  "PUT /upload" should {
    "upload file to HDFS and return Status OK" in {
      Put(s"/$apiPrefix/upload", MultipartFormData(Seq(
        BodyPart(FormFile("file name",
          HttpEntity("file content").asInstanceOf[HttpEntity.NonEmpty]), "file"))
      )) ~>
        addHeader("X-Auth-Token", correctTenantA) ~> testRoute ~> check {
        status should be(StatusCodes.OK)
        verify(fileUploadService).uploadFile(any(), any(), any())
      }
      ()
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
    when(entityService.getAllSaved(anyString())).thenReturn(Future.successful(entities.map(_.info)))
    when(entityService.deleteEntity(any(), any())).thenReturn(Future.successful(()))
    when(entityService.createEntity(any())).thenReturn(Future.successful(Entity.Id.randomId))
    when(entityService.updateEntity(any(), any(), any())).thenAnswer(
      new Answer[Future[Option[EntityWithReport]]] {
        override def answer(
            invocationOnMock: InvocationOnMock): Future[Option[EntityWithReport]] = {
          val entity = invocationOnMock.getArgumentAt(2, classOf[UpdateEntityRequest])
          val result = if (entity == notAddedEntityUpdate) None else Some(addedEntity)
          Future.successful(result)
        }
      })
    when(entityService.getEntityReport(any(), any())).thenAnswer(
      new Answer[Future[Option[EntityWithReport]]] {
        override def answer(
            invocationOnMock: InvocationOnMock): Future[Option[EntityWithReport]] = {
          val entityId = invocationOnMock.getArgumentAt(1, classOf[Entity.Id])
          val result = if (entityId == notAddedEntity.info.entityId) None else Some(addedEntity)
          Future.successful(result)
        }
      })
    entityService
  }

  private def createMockFileUploadService: FileUploadService = {
    val fileUploadService = mock[FileUploadService]
    when(fileUploadService.uploadFile(any(), any(), any()))
      .thenReturn(Future.successful(Entity.Id.randomId))
    fileUploadService
  }

  private def createRestComponent(tokenTranslator: TokenTranslator): Route = new EntitiesApi(
      tokenTranslator,
      entityService,
      fileUploadService,
      new AllAllowedAuthorizationProvider(),
      apiPrefix, "role1", "role2", "role3", "role4").route

  private class AllAllowedAuthorizationProvider extends AuthorizatorProvider {
    override def forContext(userContext: Future[UserContext]): Authorizator = {
      new Authorizator {
        override def withRole[T](role: String)(onSuccess: UserContext => Future[T]): Future[T] =
          userContext.flatMap(onSuccess)
      }
    }
  }
}
