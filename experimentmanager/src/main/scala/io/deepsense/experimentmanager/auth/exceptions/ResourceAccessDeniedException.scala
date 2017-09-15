/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Wojciech Jurczyk
 */

package io.deepsense.experimentmanager.auth.exceptions

import io.deepsense.experimentmanager.auth.HasTenantId
import io.deepsense.experimentmanager.auth.usercontext.UserContext

case class ResourceAccessDeniedException(userContext: UserContext, resource: HasTenantId)
  extends AuthException("Unauthorized user tenant " +
    s"${userContext.tenantId} != resource tenant ${resource.tenantId}")
