/**
 * Copyright 2015 deepsense.ai (CodiLime, Inc)
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

package ai.deepsense.deeplang.catalogs.doperable

import java.lang.reflect.Constructor

import ai.deepsense.deeplang.catalogs.doperable.exceptions.NoParameterlessConstructorInClassException
import ai.deepsense.deeplang.{DOperable, TypeUtils}

private[doperable] class ConcreteClassNode(javaType: Class[_]) extends ClassNode(javaType) {
  val constructor: Constructor[_] = TypeUtils.constructorForClass(javaType) match {
    case Some(parameterLessConstructor) => parameterLessConstructor
    case None => throw NoParameterlessConstructorInClassException(this.javaTypeName)
  }

  /**
   * Creates instance of type represented by this.
   * Invokes first constructor and assumes that it takes no parameters.
   */
  private[doperable] def createInstance[T <: DOperable]: T = {
    TypeUtils.createInstance[T](constructor.asInstanceOf[Constructor[T]])
  }

  override private[doperable] def subclassesInstances: Set[ConcreteClassNode] = {
    super.subclassesInstances + this
  }
}
