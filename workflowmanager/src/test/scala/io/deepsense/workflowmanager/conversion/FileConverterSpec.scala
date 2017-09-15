/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.workflowmanager.conversion

import scala.concurrent.Future

import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.WordSpecLike
import org.scalatest.concurrent.IntegrationPatience

import io.deepsense.commons.auth.usercontext.{Role, UserContext}
import io.deepsense.commons.auth.{Authorizator, AuthorizatorProvider}
import io.deepsense.commons.{StandardSpec, UnitTestSupport}
import io.deepsense.entitystorage.{EntityStorageClient, EntityStorageClientFactory}
import io.deepsense.models.entities.{DataObjectReference, Entity, EntityInfo, EntityWithData}
import io.deepsense.models.workflows.Workflow
import io.deepsense.workflowmanager.WorkflowManager
import io.deepsense.workflowmanager.exceptions.FileNotFoundException

class FileConverterSpec
  extends StandardSpec
  with UnitTestSupport
  with WordSpecLike
  with IntegrationPatience {

  val tenantId = "tenantId"
  val roleForAll = "aRole"
  val userContext = mock[UserContext]
  when(userContext.tenantId).thenReturn(tenantId)
  when(userContext.roles).thenReturn(Set(Role(roleForAll)))
  val userContextFuture: Future[UserContext] = Future.successful(userContext)

  val authorizatorProvider: AuthorizatorProvider = new AllAllowedAuthorizationProvider
  val authorizator = authorizatorProvider.forContext(userContextFuture)

  val esClientFactory = mock[EntityStorageClientFactory]
  val esClient = mock[EntityStorageClient]

  when(esClientFactory.create(any(), any(), any(), any(), any())).thenReturn(esClient)

  val entityWithData = mock[EntityWithData]
  when(entityWithData.dataReference).thenReturn(mock[DataObjectReference])
  when(entityWithData.info).thenReturn(mock[EntityInfo])

  val experiment = mock[Workflow]
  val experimentManager = mock[WorkflowManager]

  val fileConverter = new FileConverter(authorizatorProvider, esClientFactory,
    "role", "actor-system", "hostname", 0, "actor", 1)

  "FileConverter" should {

    "launch experiment and respond Success" when {
      "received Launch on experiment" in {
        when(esClient.getEntityData(any(), any())(any()))
          .thenReturn(Future.successful(Some(entityWithData)))
        when(experimentManager.create(any()))
          .thenReturn(Future.successful(experiment))
        when(experimentManager.launch(any(), any()))
          .thenReturn(Future.successful(experiment))

        whenReady(fileConverter.convert(
          Entity.Id.randomId, userContextFuture, experimentManager)) { _ =>
          verify(experimentManager).create(any())
          verify(experimentManager).launch(any(), any())
        }
        ()
      }
    }

    "respond Failed" when {

      "entity was not found" in {
        when(esClient.getEntityData(any(), any())(any()))
          .thenReturn(Future.successful(None))

        whenReady(fileConverter.convert(
          Entity.Id.randomId, userContextFuture, experimentManager).failed) { e =>
          (e.isInstanceOf[FileNotFoundException]) shouldBe true
        }
      }

      "getting entity data failed" in {
        when(esClient.getEntityData(any(), any())(any()))
          .thenReturn(Future.failed(FileNotFoundException(Entity.Id.randomId)))

        whenReady(fileConverter.convert(
          Entity.Id.randomId, userContextFuture, experimentManager).failed) { e =>
          (e.isInstanceOf[FileNotFoundException]) shouldBe true
        }
      }

      "creating experiment failed" in {
        when(esClient.getEntityData(any(), any())(any()))
          .thenReturn(Future.successful(Some(entityWithData)))
        when(experimentManager.create(any()))
          .thenReturn(Future.failed(FileNotFoundException(Entity.Id.randomId)))

        whenReady(fileConverter.convert(
          Entity.Id.randomId, userContextFuture, experimentManager).failed) { e =>
          (e.isInstanceOf[FileNotFoundException]) shouldBe true
        }
      }

      "launching experiment failed" in {
        when(esClient.getEntityData(any(), any())(any()))
          .thenReturn(Future.successful(Some(entityWithData)))
        when(experimentManager.create(any()))
          .thenReturn(Future.successful(experiment))
        when(experimentManager.launch(any(), any()))
          .thenReturn(Future.failed(FileNotFoundException(Entity.Id.randomId)))

        whenReady(fileConverter.convert(
          Entity.Id.randomId, userContextFuture, experimentManager).failed) { e =>
          (e.isInstanceOf[FileNotFoundException]) shouldBe true
        }
      }
    }
  }

  private class AllAllowedAuthorizationProvider extends AuthorizatorProvider {
    override def forContext(userContext: Future[UserContext]): Authorizator = {
      new Authorizator {
        override def withRole[T](role: String)(onSuccess: UserContext => Future[T]): Future[T] =
          userContext.flatMap(onSuccess)
      }
    }
  }
}
