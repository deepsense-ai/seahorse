/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.workflowmanager.rest

import scala.concurrent._

import org.mockito.Mockito._
import org.scalatest.Matchers
import spray.http.StatusCodes
import spray.json._
import spray.routing.Route

import io.deepsense.commons.auth.directives.AuthDirectives
import io.deepsense.commons.auth.usercontext.{TokenTranslator, UserContext}
import io.deepsense.commons.auth.{Authorizator, AuthorizatorProvider, UserContextAuthorizator}
import io.deepsense.commons.{StandardSpec, UnitTestSupport}
import io.deepsense.deeplang.DOperation
import io.deepsense.deeplang.catalogs.doperable.{ClassDescriptor, DOperableCatalog, HierarchyDescriptor, TraitDescriptor}
import io.deepsense.deeplang.catalogs.doperations.{DOperationCategory, DOperationCategoryNode, DOperationDescriptor, DOperationsCatalog}
import io.deepsense.deeplang.parameters.ParametersSchema
import io.deepsense.models.json.workflow.DeepLangJsonProtocol
import io.deepsense.models.workflows.Workflow

class OperationsApiSpec
  extends StandardSpec
  with UnitTestSupport
  with ApiSpecSupport
  with DefaultJsonProtocol
  with DeepLangJsonProtocol
  with Matchers {

  val correctTenant: String = "A"

  override val authTokens: Map[String, Set[String]] = Map(
    correctTenant -> Set()
  )
  val dOperableCatalog = mock[DOperableCatalog]

  val hierarchyDescriptorMock = HierarchyDescriptor(
    Map("test 1" -> TraitDescriptor("trait name", Nil)),
    Map("test 2" -> ClassDescriptor("class name", None, Nil)))
  when(dOperableCatalog.descriptor) thenReturn hierarchyDescriptorMock

  val dOperationsCatalog = mock[DOperationsCatalog]

  val existingOperationId = DOperation.Id.randomId
  val mockCategory = mock[DOperationCategory]
  when(mockCategory.id) thenReturn DOperationCategory.Id.randomId
  when(mockCategory.name) thenReturn "some category name"

  val existingOperationDescriptor = DOperationDescriptor(
    existingOperationId, "operation name", "operation description",
    mockCategory, ParametersSchema(), Nil, Nil)
  val envelopedExistingOperationDescription = Map("operation" -> existingOperationDescriptor)

  val operationsMapMock = Map(existingOperationId -> existingOperationDescriptor)
  when(dOperationsCatalog.operations) thenReturn operationsMapMock
  val operationsResponse = Map("operations" -> operationsMapMock)

  val categoryTreeMock = DOperationCategoryNode(Some(mockCategory), Map.empty, Set.empty)
  when(dOperationsCatalog.categoryTree) thenReturn categoryTreeMock

  override def createRestComponent(tokenTranslator: TokenTranslator): Route = {
    new SecureOperationsApi(
      tokenTranslator,
      dOperableCatalog,
      dOperationsCatalog,
      new TestAuthorizationProvider(),
      apiPrefix).route
  }

  val apiPrefix: String = "v1/operations"

  "GET /operations/hierarchy" should {
    "return dOperables hierarchy" when {
      "valid auth token was send" in {
        Get(s"/$apiPrefix/hierarchy") ~>
          addHeader("X-Auth-Token", correctTenant) ~> testRoute ~> check {
          status should be(StatusCodes.OK)
          responseAs[HierarchyDescriptor] shouldBe hierarchyDescriptorMock
        }
      }
    }
    "return Unauthorized" when {
      "no auth token was send (on MissingHeaderRejection)" in {
        Get(s"/$apiPrefix/hierarchy") ~> testRoute ~> check {
          status should be(StatusCodes.Unauthorized)
        }
      }
    }
  }

  "GET /operations" should {
    "return dOperations list" when {
      "valid auth token was send" in {
        Get(s"/$apiPrefix") ~>
          addHeader("X-Auth-Token", correctTenant) ~> testRoute ~> check {
          status should be(StatusCodes.OK)
          implicit val operationDescriptor = DOperationDescriptorBaseFormat
          responseAs[JsObject] shouldBe operationsResponse.toJson
        }
      }

      "no auth token was send (on MissingHeaderRejection)" in {
        Get(s"/$apiPrefix") ~> testRoute ~> check {
          status should be(StatusCodes.Unauthorized)
        }
      }
    }
  }

  "GET /operations/catalog" should {
    "return dOperations catalog" when {
      "valid auth token was send" in {
        Get(s"/$apiPrefix/catalog") ~>
          addHeader("X-Auth-Token", correctTenant) ~> testRoute ~> check {
          status should be(StatusCodes.OK)
          implicit val operationDescriptor = DOperationDescriptorShortFormat
          responseAs[JsObject] shouldBe dOperationsCatalog.categoryTree.toJson
        }
      }
    }
    "return Unauthorized" when {
      "no auth token was send (on MissingHeaderRejection)" in {
        Get(s"/$apiPrefix/catalog") ~> testRoute ~> check {
          status should be(StatusCodes.Unauthorized)
        }
      }
    }
  }

  "GET /operations/:id" should {
    "return existing dOperation descriptor" when {
      "valid auth token was send" in {
        Get(s"/$apiPrefix/$existingOperationId") ~>
          addHeader("X-Auth-Token", correctTenant) ~> testRoute ~> check {
          status should be(StatusCodes.OK)
          implicit val operationDescriptor = DOperationDescriptorFullFormat
          responseAs[JsObject] shouldBe envelopedExistingOperationDescription.toJson
        }
      }
    }
    "return Unauthorized" when {
      "no auth token was send (on MissingHeaderRejection)" in {
        Get(s"/$apiPrefix/catalog") ~> testRoute ~> check {
          status should be(StatusCodes.Unauthorized)
        }
      }
    }
    "return Not found" when {
      "asked for non existing Workflow" in {
        Get(s"/$apiPrefix/${Workflow.Id.randomId}") ~>
          addHeader("X-Auth-Token", correctTenant) ~> testRoute ~> check {
          status should be(StatusCodes.NotFound)
        }
      }
    }
  }

  private class TestAuthorizationProvider extends AuthorizatorProvider {
    override def forContext(userContext: Future[UserContext]): Authorizator = {
      new UserContextAuthorizator(userContext)
    }
  }
}
