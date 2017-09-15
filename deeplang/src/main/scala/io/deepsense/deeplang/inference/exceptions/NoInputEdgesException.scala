/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.inference.exceptions

import io.deepsense.deeplang.exceptions.DeepLangException

case class NoInputEdgesException(
    portIndex: Int)
  extends DeepLangException(s"No edges going to port $portIndex")
