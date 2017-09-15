/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Witold Jedrzejewski
 */

package io.deepsense.deeplang.dhierarchy

import java.lang.reflect.Constructor

import io.deepsense.deeplang.{TypeUtils, DOperable}
import io.deepsense.deeplang.dhierarchy.exceptions.NoParameterLessConstructorInClassException

private[dhierarchy] class ConcreteClassNode(javaType: Class[_]) extends ClassNode(javaType) {
  val constructor: Constructor[_] = TypeUtils.constructorForClass(javaType) match {
    case Some(parameterLessConstructor) => parameterLessConstructor
    case None => throw NoParameterLessConstructorInClassException(this)
  }

  /**
   * Creates instance of type represented by this.
   * Invokes first constructor and assumes that it takes no parameters.
   */
  private[dhierarchy] def createInstance[T <: DOperable]: T = {
    TypeUtils.createInstance[T](constructor)
  }

  override private[dhierarchy] def subclassesInstances: Set[ConcreteClassNode] = {
    super.subclassesInstances + this
  }
}
