/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Wojciech Jurczyk
 */

package io.deepsense.experimentmanager.auth

import scala.concurrent.Future

import org.mockito.Matchers._
import org.mockito.Mockito._
import spray.routing.Directives._
import spray.routing.MissingHeaderRejection

import io.deepsense.experimentmanager.auth.directives.AuthDirectives
import io.deepsense.experimentmanager.auth.usercontext.{TokenTranslator, UserContext}
import io.deepsense.experimentmanager.{StandardSpec, UnitTestSupport}


class WithUserContextSpec
  extends StandardSpec
  with UnitTestSupport
  with AuthDirectives {

  val tokenTranslator = mock[TokenTranslator]
  when(tokenTranslator.translate(any(classOf[String])))
    .thenReturn(Future.successful(mock[UserContext]))

  "withUserDirective" should {
    "reject" when {
      "no auth token was send" in {
        Get("/") ~> withUserContext(x => complete(x.map(_.toString))) ~> check {
          handled shouldBe false
          rejection shouldBe MissingHeaderRejection(TokenHeader)
        }
      }
    }
    "create a future context" when {
      "auth token was send" in {
        val headerValue = "Foo"
        Get("/") ~> addHeader(TokenHeader, headerValue) ~>
          withUserContext(x => complete(x.map(_.toString))) ~>
          check {
            handled shouldBe true
            verify(tokenTranslator).translate(headerValue)
        }
      }
    }
  }
}
