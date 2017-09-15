/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang

import io.deepsense.deeplang.catalogs.doperable.DOperableCatalog

/**
 * Holds information needed by DOperations and DMethods during knowledge inference.
 * @param dOperableCatalog object responsible for registering and validating the type hierarchy
 * @param fullInference if set to true, infer metadata and types; otherwise infer the types only
 */
class InferContext(
    val dOperableCatalog: DOperableCatalog,
    val fullInference: Boolean = false)
  extends CommonContext
