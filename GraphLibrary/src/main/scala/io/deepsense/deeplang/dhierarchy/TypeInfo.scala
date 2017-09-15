/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Witold Jedrzejewski
 */

package io.deepsense.deeplang.dhierarchy

trait TypeInfo

case class ClassInfo(
    name: String,
    parent: Option[String],
    traits: List[String])
  extends TypeInfo

case class TraitInfo(
    name: String,
    parents: List[String])
  extends TypeInfo
