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

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{Await, Future, duration}
import scala.util.Failure

import org.mockito.Mockito._

import ai.deepsense.commons.{StandardSpec, UnitTestSupport}
import ai.deepsense.commons.auth.exceptions.NoRoleException
import ai.deepsense.commons.auth.usercontext.{Role, UserContext}

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
