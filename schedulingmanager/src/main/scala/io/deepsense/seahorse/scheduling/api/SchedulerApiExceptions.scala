/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.seahorse.scheduling.api

import io.deepsense.commons.service.api.CommonApiExceptions.ApiException

object SchedulerApiExceptions {

  def schedulerError = ApiException(
    message = "Unexpected Quartz error occured",
    errorCode = 500
  )
}
