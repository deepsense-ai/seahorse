/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Witold Jedrzejewski
 */

package io.deepsense.deeplang.catalogs.doperable

import scala.reflect.runtime.{universe => ru}

import io.deepsense.deeplang.TypeUtils

/**
 * Node that represents class in hierarchy stored in DOperableCatalog.
 */
private[doperable] class ClassNode(protected override val javaType: Class[_]) extends TypeNode {

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
