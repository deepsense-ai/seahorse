/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Witold Jedrzejewski
 */

package io.deepsense.deeplang

import java.lang.reflect.Constructor

import scala.reflect.runtime.{universe => ru}

/**
 * Holds methods used for manipulating objects representing types.
 */
private[deeplang] object TypeUtils {
  private val mirror = ru.runtimeMirror(getClass.getClassLoader)

  def classToType(c: Class[_]): ru.Type = mirror.classSymbol(c).toType

  def typeToClass(t: ru.Type): Class[_] = mirror.runtimeClass(t.typeSymbol.asClass)

  def symbolToType(s: ru.Symbol): ru.Type = s.asClass.toType

  def isParametrized(t: ru.Type): Boolean = t.typeSymbol.asClass.typeParams.nonEmpty

  def isAbstract(c: Class[_]): Boolean = classToType(c).typeSymbol.asClass.isAbstract

  def constructorForClass(c: Class[_]): Option[Constructor[_]] = {
    val constructors = c.getConstructors
    val isParameterLess: (Constructor[_] => Boolean) = constructor =>
      constructor.getParameterTypes.length == 0
    constructors.find(isParameterLess)
  }

  def constructorForType(t: ru.Type): Option[Constructor[_]] = {
    constructorForClass(typeToClass(t))
  }

  def createInstance[T](constructor: Constructor[_]) = {
    constructor.newInstance().asInstanceOf[T]
  }
}
