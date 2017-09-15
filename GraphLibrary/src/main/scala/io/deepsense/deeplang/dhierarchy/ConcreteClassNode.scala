/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Witold Jedrzejewski
 */

package io.deepsense.deeplang.dhierarchy

import java.lang.reflect.Constructor

import scala.collection.mutable

import io.deepsense.deeplang.DOperable
import io.deepsense.deeplang.dhierarchy.exceptions.NoParameterLessConstructorException

private[dhierarchy] class ConcreteClassNode(javaType: Class[_]) extends ClassNode(javaType) {
  val constructor: Constructor[_] = {
    val constructors = javaType.getConstructors
    val isParameterLess: (Constructor[_] => Boolean) = constructor =>
      constructor.getParameterTypes.length == 0
    constructors.find(isParameterLess) match {
      case Some(parameterLessConstructor) => parameterLessConstructor
      case None => throw new NoParameterLessConstructorException(this)
    }
  }

  /**
   * Creates instance of type represented by this.
   * Invokes first constructor and assumes that it takes no parameters.
   */
  private[dhierarchy] def createInstance(): DOperable = {
    constructor.newInstance().asInstanceOf[DOperable]
  }

  override private[dhierarchy] def subclassesInstances: mutable.Set[ConcreteClassNode] = {
    super.subclassesInstances + this
  }
}
