/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Witold Jedrzejewski
 */

package io.deepsense.deeplang.dhierarchy

import io.deepsense.deeplang.TypeUtils
import io.deepsense.deeplang.dhierarchy.exceptions.TraitInheritingFromClassException

import scala.reflect.runtime.{universe => ru}

/**
 * Represents Trait in DHierarchy graph.
 */
private[dhierarchy] class TraitNode(protected override val javaType: Class[_]) extends Node {

  private[dhierarchy] override def addParent(node: Node): Unit = {
    throw new TraitInheritingFromClassException(this, node)
  }

  private[dhierarchy] override def getParentJavaType(upperBoundType: ru.Type): Option[Class[_]] = {
    val t = TypeUtils.classToType(javaType)
    val baseTypes = t.baseClasses.map(TypeUtils.symbolToType)
    val baseJavaTypes = baseTypes.filter(_ <:< upperBoundType).map(TypeUtils.typeToClass)
    baseJavaTypes.find(!_.isInterface)
  }

  private[dhierarchy] override def info: TypeInfo = {
    TraitInfo(displayName, supertraits.values.map(_.displayName).toList)
  }

  override def toString = s"DTrait($fullName)"
}

private[dhierarchy] object TraitNode {
  def apply(javaType: Class[_]): TraitNode = new TraitNode(javaType)
}
