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

package ai.deepsense.workflowmanager.rest

import scala.concurrent.Future

import org.mockito.Matchers._
import org.mockito.Mockito._
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import org.scalatest.mockito.MockitoSugar
import spray.routing.Route

import ai.deepsense.commons.auth.usercontext.{CannotGetUserException, Role, TokenTranslator, UserContext}

trait ApiSpecSupport extends MockitoSugar {

  val authTokens: Map[String, Set[String]]

  val apiPrefix: String

  def createRestComponent(tokenTranslator: TokenTranslator): Route

  protected def testRoute: Route = {
    val tokenTranslator = mock[TokenTranslator]
    when(tokenTranslator.translate(any(classOf[String])))
      .thenAnswer(new Answer[Future[UserContext]] {
      override def answer(invocation: InvocationOnMock): Future[UserContext] = {
        val tokenFromRequest = invocation.getArgumentAt(0, classOf[String])
        if (authTokens.keySet.contains(tokenFromRequest)) {
          val uc = mockUserContext(tokenFromRequest)
          Future.successful(uc)
        } else {
          Future.failed(new CannotGetUserException(tokenFromRequest))
        }
      }
    })
    createRestComponent(tokenTranslator)
  }

  private def mockUserContext(tenantId: String): UserContext = {
    val userContext = mock[UserContext]
    when(userContext.tenantId).thenReturn(tenantId)
    when(userContext.roles).thenReturn(authTokens(tenantId).map(Role(_)))
    userContext
  }
}
