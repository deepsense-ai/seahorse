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

import scala.reflect.runtime.{universe => ru}

import ai.deepsense.deeplang.TypeUtils

/**
 * Node that represents class in hierarchy stored in DOperableCatalog.
 */
private[doperable] class ClassNode(protected override val javaType: Class[_]) extends TypeNode {

  def javaTypeName: String = javaType.getCanonicalName

  private[doperable] override def getParentJavaType(upperBoundType: ru.Type): Option[Class[_]] = {
    val parentJavaType = javaType.getSuperclass
    val parentType = TypeUtils.classToType(parentJavaType)
    if (parentType <:< upperBoundType) Some(parentJavaType) else None
  }

  private[doperable] override def descriptor: TypeDescriptor = {
    val parentName = if (parent.isDefined) Some(parent.get.fullName) else None
    ClassDescriptor(fullName, parentName, supertraits.values.map(_.fullName).toList)
  }
}

private[doperable] object ClassNode {
  def apply(javaType: Class[_]): ClassNode =
    if (TypeUtils.isAbstract(javaType)) {
      new ClassNode(javaType)
    } else {
      new ConcreteClassNode(javaType)
    }
}
