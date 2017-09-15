/**
 * Copyright 2015 deepsense.ai (CodiLime, Inc)
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

package ai.deepsense.commons.auth

import scala.concurrent.Future

import org.mockito.Matchers._
import org.mockito.Mockito._
import spray.routing.Directives._
import spray.routing.MissingHeaderRejection

import ai.deepsense.commons.{StandardSpec, UnitTestSupport}
import ai.deepsense.commons.auth.directives.AuthDirectives
import ai.deepsense.commons.auth.usercontext.{TokenTranslator, UserContext}

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
