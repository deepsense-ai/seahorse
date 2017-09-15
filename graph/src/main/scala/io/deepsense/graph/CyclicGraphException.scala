/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.graph

import io.deepsense.commons.exception.{FailureCode, DeepSenseException}

class CyclicGraphException
  extends DeepSenseException(
    FailureCode.IllegalArgumentException,
    "Cyclic graph",
    "Graph cycle detected") {

}
