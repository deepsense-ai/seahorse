/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Witold Jedrzejewski
 */

package io.deepsense.deeplang.dhierarchy.exceptions

import io.deepsense.deeplang.dhierarchy.ConcreteClassNode

case class NoParameterLessConstructorInClassException(classNode: ConcreteClassNode)
  extends DHierarchyException(s"Concrete class registered in DHierarchy has to have" +
    s" parameter-less constructor ($classNode has no parameter-less constructor)")
