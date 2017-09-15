/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Witold Jedrzejewski
 */

package io.deepsense.deeplang.catalogs.doperable.exceptions

import io.deepsense.deeplang.catalogs.doperable.ConcreteClassNode

case class NoParameterlessConstructorInClassException(classNode: ConcreteClassNode)
  extends DOperableCatalogException(s"Concrete class registered in DHierarchy has to have" +
    s" parameterless constructor ($classNode has no parameterless constructor)")
