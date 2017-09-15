/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.parameters

object ParameterType extends Enumeration {
  type ParameterType = Value
  val Boolean = Value("boolean")
  val Numeric = Value("numeric")
  val String = Value("string")
  val Choice = Value("choice")
  val MultipleChoice = Value("multipleChoice")
  val Multiplier = Value("multiplier")
  val ColumnSelector = Value("selector")
  val SingleColumnCreator = Value("creator")
  val MultipleColumnCreator = Value("multipleCreator")
  val PrefixBasedColumnCreator = Value("prefixBasedCreator")
}
