/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Witold Jedrzejewski
 */

package io.deepsense.deeplang.dhierarchy.exceptions

import io.deepsense.deeplang.dhierarchy.{TraitNode, Node}

class TraitInheritingFromClassException(
    val traitNode: TraitNode,
    val classNode: Node)
  extends DHierarchyException(
    s"DTrait cannot inherit from DClass in DHierarchy, ($traitNode inherits from $classNode)")
