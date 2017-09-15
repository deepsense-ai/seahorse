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
 * Represents Trait in DHierarchy graph.
 */
private[doperable] class TraitNode(protected override val javaType: Class[_]) extends Node {

  private[doperable] override def addParent(node: Node): Unit = {
    throw new TraitInheritingFromClassException(this, node)
  }

  private[doperable] override def getParentJavaType(upperBoundType: ru.Type): Option[Class[_]] = {
    val t = TypeUtils.classToType(javaType)
    val baseTypes = t.baseClasses.map(TypeUtils.symbolToType)
    val baseJavaTypes = baseTypes.filter(_ <:< upperBoundType).map(TypeUtils.typeToClass)
    baseJavaTypes.find(!_.isInterface)
  }

  private[doperable] override def descriptor: TypeDescriptor = {
    TraitDescriptor(displayName, supertraits.values.map(_.displayName).toList)
  }

  override def toString = s"DTrait($fullName)"
}

private[doperable] object TraitNode {
  def apply(javaType: Class[_]): TraitNode = new TraitNode(javaType)
}
