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

import org.mockito.Mockito._

import ai.deepsense.commons.{StandardSpec, UnitTestSupport}
import ai.deepsense.commons.auth.exceptions.ResourceAccessDeniedException
import ai.deepsense.commons.auth.usercontext.UserContext

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
