/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Witold Jedrzejewski
 */

package io.deepsense.deeplang.dhierarchy.exceptions

import scala.reflect.runtime.{universe => ru}

import io.deepsense.deeplang.dhierarchy.ClassNode

class NoParameterLessConstructorException(classNode: ClassNode)
    extends DHierarchyException(s"Concrete class registered in DHierarchy has to have" +
        " parameter-less constructor ($classNode has no parameter-less constructor)")
