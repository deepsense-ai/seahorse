/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.workflowmanager.rest

import scala.concurrent.Future

import org.mockito.Matchers._
import org.mockito.Mockito._
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import org.scalatest.mock.MockitoSugar
import spray.routing.Route

import io.deepsense.commons.auth.usercontext.{CannotGetUserException, Role, TokenTranslator, UserContext}

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
