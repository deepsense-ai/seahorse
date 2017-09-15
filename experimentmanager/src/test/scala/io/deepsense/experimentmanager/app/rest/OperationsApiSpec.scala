/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Rafal Hryciuk
 */

package io.deepsense.experimentmanager.app.rest

import java.util.UUID

import scala.concurrent._

import spray.http.StatusCodes
import spray.json.JsValue
import spray.routing.Route

import io.deepsense.deeplang.catalogs.doperable.{HierarchyDescriptor, DOperableCatalog}
import io.deepsense.deeplang.catalogs.doperations.{DOperationCategoryNode, DOperationDescriptor, DOperationsCatalog}
import io.deepsense.experimentmanager.app.rest.json.DOperationDescriptorJsonProtocol
import io.deepsense.experimentmanager.app.rest.json.RestJsonProtocol._
import io.deepsense.experimentmanager.auth.usercontext.{TokenTranslator, UserContext}
import io.deepsense.experimentmanager.auth.{Authorizator, AuthorizatorProvider, UserContextAuthorizator}
import io.deepsense.experimentmanager.{CatalogRecorder, StandardSpec, UnitTestSupport}

class OperationsApiSpec extends StandardSpec with UnitTestSupport with ApiSpecSupport {

  val correctTenant: String = "A"

  override val authTokens: Map[String, Set[String]] = Map(
    correctTenant -> Set()
  )

  override def createRestComponent(tokenTranslator: TokenTranslator): Route  = {
    val dOperableCatalog = new DOperableCatalog()
    val dOperationsCatalog = DOperationsCatalog()
    CatalogRecorder.registerDOperables(dOperableCatalog)
    new OperationsApi(
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
          responseAs[HierarchyDescriptor]
        }
      }

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
          implicit val expectedDescriptorsFormat = DOperationDescriptorJsonProtocol.BaseFormat
          responseAs[Map[UUID, DOperationDescriptor]]
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
          responseAs[Map[String, JsValue]]
        }
      }

      "no auth token was send (on MissingHeaderRejection)" in {
        Get(s"/$apiPrefix/catalog") ~> testRoute ~> check {
          status should be(StatusCodes.Unauthorized)
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
