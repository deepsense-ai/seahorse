/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.doperations.exceptions

import io.deepsense.deeplang.exceptions.DeepLangException

abstract class DOperationExecutionException(message: String, cause: Option[Throwable])
  extends DeepLangException(message, cause.orNull)
