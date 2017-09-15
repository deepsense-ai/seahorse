/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Witold Jedrzejewski
 */

package io.deepsense.deeplang.dhierarchy

/**
 * Represents Trait in DHierarchy graph.
 */
private[dhierarchy] class TraitNode(protected override val typeInfo: Class[_]) extends Node {

  private[dhierarchy] override def addParent(node: Node): Unit = {
    throw new RuntimeException // TODO: What is the exceptions convention here?
  }

  private[dhierarchy] override def info: TypeInfo = {
    TraitInfo(displayName, supertraits.values.map(_.displayName).toList)
  }
}
