/**
 * Copyright (c) 2015, CodiLime, Inc.
 */

package io.deepsense.commons.auth.exceptions

import io.deepsense.commons.auth.HasTenantId
import io.deepsense.commons.auth.usercontext.UserContext

case class ResourceAccessDeniedException(userContext: UserContext, resource: HasTenantId)
  extends AuthException("Unauthorized user tenant " +
    s"${userContext.tenantId} != resource tenant ${resource.tenantId}")
