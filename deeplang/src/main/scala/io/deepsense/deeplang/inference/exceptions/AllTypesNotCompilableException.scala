/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.inference.exceptions

import io.deepsense.deeplang.exceptions.DeepLangException

case class AllTypesNotCompilableException(
    portIndex: Int)
  extends DeepLangException(s"None of provided types can be put in port $portIndex")
