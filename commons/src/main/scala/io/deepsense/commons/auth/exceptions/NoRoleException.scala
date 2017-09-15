/**
 * Copyright (c) 2015, CodiLime, Inc.
 */

package io.deepsense.commons.auth.exceptions

import io.deepsense.commons.auth.usercontext.UserContext

case class NoRoleException(userContext: UserContext, expectedRole: String)
  extends AuthException(s"No role $expectedRole in userContext = $userContext")
