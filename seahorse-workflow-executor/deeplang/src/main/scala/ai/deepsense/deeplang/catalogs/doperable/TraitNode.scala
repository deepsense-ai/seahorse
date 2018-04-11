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
 * Node that represents trait in hierarchy stored in DOperableCatalog.
 */
private[doperable] class TraitNode(protected override val javaType: Class[_]) extends TypeNode {

  private[doperable] override def getParentJavaType(upperBoundType: ru.Type): Option[Class[_]] = {
    val t = TypeUtils.classToType(javaType)
    val baseTypes = t.baseClasses.map(TypeUtils.symbolToType)
    val mirror = TypeUtils.classMirror(javaType)
    val baseJavaTypes = baseTypes.filter(_ <:< upperBoundType).map(TypeUtils.typeToClass(_, mirror))
    baseJavaTypes.find(!_.isInterface)
  }

  private[doperable] override def descriptor: TypeDescriptor = {
    TraitDescriptor(fullName, (supertraits.values ++ parent).map(_.fullName).toList)
  }
}

private[doperable] object TraitNode {
  def apply(javaType: Class[_]): TraitNode = new TraitNode(javaType)
}
