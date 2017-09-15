/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Witold Jedrzejewski
 */

package io.deepsense.deeplang.dhierarchy

import io.deepsense.deeplang.dhierarchy.exceptions.TraitInheritingFromClassException

/**
 * Represents Trait in DHierarchy graph.
 */
private[dhierarchy] class TraitNode(protected override val typeInfo: Class[_]) extends Node {

  private[dhierarchy] override def addParent(node: Node): Unit = {
    throw new TraitInheritingFromClassException(this, node)
  }

  private[dhierarchy] override def info: TypeInfo = {
    TraitInfo(displayName, supertraits.values.map(_.displayName).toList)
  }

  override def toString = s"DTrait($fullName)"
}
