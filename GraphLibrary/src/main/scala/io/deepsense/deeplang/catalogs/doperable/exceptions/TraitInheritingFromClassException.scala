/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Witold Jedrzejewski
 */

package io.deepsense.deeplang.catalogs.doperable.exceptions

import io.deepsense.deeplang.catalogs.doperable.{Node, TraitNode}

case class TraitInheritingFromClassException(
    traitNode: TraitNode,
    classNode: Node)
  extends DOperableCatalogException(
    s"DTrait cannot inherit from DClass in DHierarchy, ($traitNode inherits from $classNode)")
