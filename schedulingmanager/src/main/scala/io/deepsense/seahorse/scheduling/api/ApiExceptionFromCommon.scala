/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.seahorse.scheduling.api

import io.deepsense.commons.service.api.CommonApiExceptions

object ApiExceptionFromCommon {

  def apply(ex: CommonApiExceptions.ApiException) =
    new ApiExceptionWithJsonBody(ex.message, ex.errorCode)

}
