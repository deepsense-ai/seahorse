/**
 * Copyright (c) 2015, CodiLime, Inc.
 */

package io.deepsense.deeplang.catalogs.doperable

/**
 * Describes either class or trait.
 */
trait TypeDescriptor

/**
 * Describes class.
 * @param name display name of class
 * @param parent direct superclass of described class, if any
 * @param traits all direct supertraits of described class
 */
case class ClassDescriptor(
    name: String,
    parent: Option[String],
    traits: List[String])
  extends TypeDescriptor

/**
 * Describes trait.
 * @param name display name of trait
 * @param parents all direct supertraits of described trait
 */
case class TraitDescriptor(
    name: String,
    parents: List[String])
  extends TypeDescriptor
