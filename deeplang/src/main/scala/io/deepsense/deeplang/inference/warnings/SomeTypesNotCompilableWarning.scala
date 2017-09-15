/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.inference.warnings

import io.deepsense.deeplang.inference.InferenceWarning

case class SomeTypesNotCompilableWarning(
    portIndex: Int)
  extends InferenceWarning(s"Some types can be put in port $portIndex")
