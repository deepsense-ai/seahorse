/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Wojciech Jurczyk
 */

package io.deepsense.experimentmanager.auth

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{duration, Await, Future}
import scala.util.Failure

import org.mockito.Mockito._

import io.deepsense.experimentmanager.auth.exceptions.NoRoleException
import io.deepsense.experimentmanager.auth.usercontext.{Role, UserContext}
import io.deepsense.experimentmanager.{UnitTestSupport, StandardSpec}

class UserContextAuthorizatorSpec extends StandardSpec with UnitTestSupport {

  "UserContextAuthorizator" should {
    "pass failure and do not run" when {
      "userContext future failed" in {
        val failure = new Exception
        val failedUserContext = Future.failed(failure)
        val authorizatorFailedContext = new UserContextAuthorizator(failedUserContext)
        val shouldBeFailed = authorizatorFailedContext.withRole("some-role") { context =>
          Future.successful("this should not run")
        }
        Await.ready(shouldBeFailed, new FiniteDuration(2, duration.SECONDS))
        shouldBeFailed.value.get match {
          case Failure(f) if f == failure => Unit
          case _ => fail("Future should be failed")
        }
      }
    }
    "authorize when role exists" in {
      val workingRole = "mock"
      val roles = Set(
        Role("nameA"),
        Role(workingRole),
        Role("nameC"),
        Role("nameD")
      )
      val userContext = mock[UserContext]
      when(userContext.roles).thenReturn(roles)
      val authorizator = new UserContextAuthorizator(Future.successful(userContext))

      val resultForRole = authorizator.withRole(workingRole) { userContext =>
        Future.successful(Unit)
      }

      Await.ready(resultForRole, new FiniteDuration(2, duration.SECONDS))
      resultForRole.value.get.isSuccess shouldBe true
    }
    "fail with NoRoleException" when {
      "user does not have expected role" in {
        val nonExistingRole = "anyrole"
        val userContext = mock[UserContext]
        when(userContext.roles).thenReturn(Set[Role]())
        val authorizator = new UserContextAuthorizator(Future.successful(userContext))
        val shouldBeFailed = authorizator.withRole(nonExistingRole) { userContext =>
          Future.successful(Unit)
        }
        Await.ready(shouldBeFailed, new FiniteDuration(2, duration.SECONDS))
        shouldBeFailed.value.get match {
          case Failure(f) if f == NoRoleException(userContext, nonExistingRole) => Unit
          case _ => fail("Future should be failed")
        }
      }
    }
  }
}
