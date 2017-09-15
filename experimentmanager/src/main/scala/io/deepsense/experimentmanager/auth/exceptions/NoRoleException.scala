/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Wojciech Jurczyk
 */

package io.deepsense.experimentmanager.auth.exceptions

import io.deepsense.experimentmanager.auth.usercontext.UserContext

case class NoRoleException(userContext: UserContext, expectedRole: String)
  extends AuthException(s"No role $expectedRole in userContext = $userContext")
