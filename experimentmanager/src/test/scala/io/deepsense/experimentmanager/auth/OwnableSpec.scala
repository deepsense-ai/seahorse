/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Wojciech Jurczyk
 */

package io.deepsense.experimentmanager.auth

import org.mockito.Mockito._

import io.deepsense.experimentmanager.auth.exceptions.ResourceAccessDeniedException
import io.deepsense.experimentmanager.auth.usercontext.UserContext
import io.deepsense.experimentmanager.{StandardSpec, UnitTestSupport}

class OwnableSpec extends StandardSpec with UnitTestSupport {

  case class TestOwnable(tenantId: String) extends Ownable

  val tenantId = "theSameForAll"
  val testUserContext = mock[UserContext]
  when(testUserContext.tenantId).thenReturn(tenantId)

  "Ownable's assertOwnedBy" should {
    "return that ownable" when {
      "the tenant owns it" in {
        val testOwnable = TestOwnable(tenantId)
        assert(testOwnable.assureOwnedBy(testUserContext) == testOwnable)
      }
    }
    "throw ResourceAccessDeniedException" when {
      "the tenant does not own the ownable" in {
        val testOwnable = TestOwnable(tenantId + tenantId) // So the id is different.
        val thrown = the [ResourceAccessDeniedException] thrownBy
            testOwnable.assureOwnedBy(testUserContext)
        assert(thrown.resource == testOwnable)
        assert(thrown.userContext == testUserContext)
      }
    }
  }
}
