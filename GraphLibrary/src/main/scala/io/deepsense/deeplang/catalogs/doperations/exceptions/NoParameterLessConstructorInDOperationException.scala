/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Witold Jedrzejewski
 */

package io.deepsense.deeplang.catalogs.doperations.exceptions

import scala.reflect.runtime.universe.Type

case class NoParameterLessConstructorInDOperationException(operationType: Type)
  extends DOperationsCatalogException(
      "Registered DOperation has to have parameter-less constructor" +
      s"(DOperation ${operationType.typeSymbol.name} has no parameter-less constructor)")
