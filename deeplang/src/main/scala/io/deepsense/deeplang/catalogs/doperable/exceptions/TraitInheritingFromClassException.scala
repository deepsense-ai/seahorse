/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Witold Jedrzejewski
 */

package io.deepsense.deeplang.catalogs.doperable.exceptions

import io.deepsense.deeplang.catalogs.doperable.{TraitNode, TypeNode}

case class TraitInheritingFromClassException(
    traitNode: TraitNode,
    classNode: TypeNode)
  extends DOperableCatalogException(
    s"Trait cannot inherit from class in hierarchy, ($traitNode inherits from $classNode)")
