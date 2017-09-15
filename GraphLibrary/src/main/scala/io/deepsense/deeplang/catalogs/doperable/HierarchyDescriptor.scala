/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Witold Jedrzejewski
 */

package io.deepsense.deeplang.catalogs.doperable

case class HierarchyDescriptor(
    traits: Iterable[TypeDescriptor],
    classes: Iterable[TypeDescriptor])

trait TypeDescriptor

case class ClassDescriptor(
    name: String,
    parent: Option[String],
    traits: List[String])
  extends TypeDescriptor

case class TraitDescriptor(
    name: String,
    parents: List[String])
  extends TypeDescriptor
