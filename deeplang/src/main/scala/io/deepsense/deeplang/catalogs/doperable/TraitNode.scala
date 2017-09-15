/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Witold Jedrzejewski
 */

package io.deepsense.deeplang.catalogs.doperable

import scala.reflect.runtime.{universe => ru}

import io.deepsense.deeplang.TypeUtils
import io.deepsense.deeplang.catalogs.doperable.exceptions.TraitInheritingFromClassException

/**
 * Node that represents trait in hierarchy stored in DOperableCatalog.
 */
private[doperable] class TraitNode(protected override val javaType: Class[_]) extends TypeNode {

  private[doperable] override def setParent(node: TypeNode): Unit = {
    throw new TraitInheritingFromClassException(this, node)
  }

  private[doperable] override def getParentJavaType(upperBoundType: ru.Type): Option[Class[_]] = {
    val t = TypeUtils.classToType(javaType)
    val baseTypes = t.baseClasses.map(TypeUtils.symbolToType)
    val baseJavaTypes = baseTypes.filter(_ <:< upperBoundType).map(TypeUtils.typeToClass)
    baseJavaTypes.find(!_.isInterface)
  }

  private[doperable] override def descriptor: TypeDescriptor = {
    TraitDescriptor(fullName, supertraits.values.map(_.fullName).toList)
  }
}

private[doperable] object TraitNode {
  def apply(javaType: Class[_]): TraitNode = new TraitNode(javaType)
}
