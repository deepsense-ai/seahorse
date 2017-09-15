/**
 * Copyright (c) 2015, CodiLime, Inc.
 */

package io.deepsense.deeplang.catalogs.doperable

import java.lang.reflect.Constructor

import io.deepsense.deeplang.catalogs.doperable.exceptions.NoParameterlessConstructorInClassException
import io.deepsense.deeplang.{DOperable, TypeUtils}

private[doperable] class ConcreteClassNode(javaType: Class[_]) extends ClassNode(javaType) {
  val constructor: Constructor[_] = TypeUtils.constructorForClass(javaType) match {
    case Some(parameterLessConstructor) => parameterLessConstructor
    case None => throw NoParameterlessConstructorInClassException(this)
  }

  /**
   * Creates instance of type represented by this.
   * Invokes first constructor and assumes that it takes no parameters.
   */
  private[doperable] def createInstance[T <: DOperable]: T = {
    TypeUtils.createInstance[T](constructor)
  }

  override private[doperable] def subclassesInstances: Set[ConcreteClassNode] = {
    super.subclassesInstances + this
  }
}
