/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Rafal Hryciuk
 */

package io.deepsense.experimentmanager.app.rest

import scala.concurrent._

import spray.http.StatusCodes
import spray.routing.Route

import io.deepsense.deeplang.catalogs.doperable.{HierarchyDescriptor, DOperableCatalog}
import io.deepsense.experimentmanager.app.rest.json.RestJsonProtocol._
import io.deepsense.experimentmanager.auth.usercontext.{TokenTranslator, UserContext}
import io.deepsense.experimentmanager.auth.{Authorizator, AuthorizatorProvider, UserContextAuthorizator}
import io.deepsense.experimentmanager.{CatalogRecorder, StandardSpec, UnitTestSupport}

class OperationsApiSpec extends StandardSpec with UnitTestSupport with ApiSpecSupport {

  val correctRole = "correctRole"
  val correctTenant: String = "A"
  val tenantWithoutRoles: String = "B"

  override val authTokens: Map[String, Set[String]] = Map(
    correctTenant -> Set(correctRole),
    tenantWithoutRoles -> Set()
  )

  override def createRestComponent(tokenTranslator: TokenTranslator): Route  = {
    val catalog: DOperableCatalog = new DOperableCatalog()
    CatalogRecorder.registerDOperable(catalog)
    new OperationsApi(
      tokenTranslator,
      catalog,
      new TestAuthorizationProvider(),
      correctRole,
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
    }

    "return Unauthorized" when {
      "invalid auth token was send (when InvalidTokenException occures)" in {
        Get(s"/$apiPrefix/hierarchy") ~>
          addHeader("X-Auth-Token", "its-invalid!") ~> testRoute ~> check {
          status should be(StatusCodes.Unauthorized)
        }
      }

      "the user does not have the requested role (on NoRoleExeption)" in {
        Get(s"/$apiPrefix/hierarchy") ~>
          addHeader("X-Auth-Token", tenantWithoutRoles) ~> testRoute ~> check {
          status should be(StatusCodes.Unauthorized)
        }
      }

      "no auth token was send (on MissingHeaderRejection)" in {
        Get(s"/$apiPrefix/hierarchy") ~> testRoute ~> check {
          status should be(StatusCodes.Unauthorized)
        }
      }
    }
  }

  private class TestAuthorizationProvider extends AuthorizatorProvider {
    override def forContext(userContext: Future[UserContext]): Authorizator
        = new UserContextAuthorizator(userContext)
  }
}
