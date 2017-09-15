/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Witold Jedrzejewski
 */

package io.deepsense.deeplang.dhierarchy

/**
 * Represents Class in DHierarchy graph.
 */
private[dhierarchy] class ClassNode(protected override val typeInfo: Class[_]) extends Node {

  private[dhierarchy] override def info: TypeInfo = {
    val parentName = if (parent.isDefined) Some(parent.get.displayName) else None
    ClassInfo(displayName, parentName, supertraits.values.map(_.displayName).toList)
  }
}
