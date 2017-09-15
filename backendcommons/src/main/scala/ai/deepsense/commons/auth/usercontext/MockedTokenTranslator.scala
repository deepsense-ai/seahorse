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

package ai.deepsense.commons.auth.usercontext

import scala.concurrent.Future

/**
 * Class for mocking security(keystone) in Deepsense.
 * Use token `godmode` to become authenticated as a user with all privileges.
 * With `godmode` token tenant: "olympus" and user: `Zeus` are associated.
 */
class MockedTokenTranslator extends TokenTranslator {

  override def translate(token: String): Future[UserContext] = {
    if ("godmode".equalsIgnoreCase(token)) {
      val tenantId = "olympus"
      val tenant: Tenant = Tenant(tenantId, tenantId, tenantId, Some(true))
      val godsRoles = Seq(
        "workflows:get",
        "workflows:update",
        "workflows:create",
        "workflows:list",
        "workflows:delete",
        "workflows:launch",
        "workflows:abort",
        "entities:get",
        "entities:create",
        "entities:update",
        "entities:delete",
        "admin")
      Future.successful(
        UserContextStruct(
          Token("godmode", Some(tenant)),
          tenant,
          User(
            id = "Zeus",
            name = "Zeus",
            email = None,
            enabled = Some(true),
            tenantId = Some(tenantId)),
          godsRoles.map(Role(_)).toSet
        ))
    } else {
      Future.failed(new RuntimeException("Unknown token"))
    }
  }
}

case class UserContextStruct(
  token: Token,
  tenant: Tenant,
  user: User,
  roles: Set[Role]) extends UserContext
