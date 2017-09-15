/**
 * Copyright 2015, CodiLime Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

  def createInstance[T](constructor: Constructor[_]): T = {
    constructor.newInstance().asInstanceOf[T]
  }
}
