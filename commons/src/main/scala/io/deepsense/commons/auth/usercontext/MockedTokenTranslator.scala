/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.commons.auth.usercontext

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
