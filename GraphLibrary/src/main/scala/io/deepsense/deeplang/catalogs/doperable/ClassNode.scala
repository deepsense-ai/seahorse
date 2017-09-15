/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Witold Jedrzejewski
 */

package io.deepsense.deeplang.catalogs.doperable

import scala.reflect.runtime.{universe => ru}

import io.deepsense.deeplang.TypeUtils

/**
 * Represents Class in DHierarchy graph.
 */
private[doperable] class ClassNode(protected override val javaType: Class[_]) extends Node {

  private[doperable] override def getParentJavaType(upperBoundType: ru.Type): Option[Class[_]] = {
    val parentJavaType = javaType.getSuperclass
    val parentType = TypeUtils.classToType(parentJavaType)
    if (parentType <:< upperBoundType) Some(parentJavaType) else None
  }

  private[doperable] override def descriptor: TypeDescriptor = {
    val parentName = if (parent.isDefined) Some(parent.get.displayName) else None
    ClassDescriptor(displayName, parentName, supertraits.values.map(_.displayName).toList)
  }

  override def toString: String = s"DClass($fullName)"
}

private[doperable] object ClassNode {
  def apply(javaType: Class[_]): ClassNode = {
    if (TypeUtils.isAbstract(javaType))
      new ClassNode(javaType)
    else
      new ConcreteClassNode(javaType)
  }
}
