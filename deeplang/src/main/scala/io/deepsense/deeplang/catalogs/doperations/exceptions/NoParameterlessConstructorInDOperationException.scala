/**
 * Copyright (c) 2015, CodiLime, Inc.
 */

package io.deepsense.deeplang.catalogs.doperations.exceptions

import scala.reflect.runtime.universe.Type

case class NoParameterlessConstructorInDOperationException(operationType: Type)
  extends DOperationsCatalogException(
      "Registered DOperation has to have parameterless constructor" +
      s"(DOperation ${operationType.typeSymbol.name} has no parameterless constructor)")
