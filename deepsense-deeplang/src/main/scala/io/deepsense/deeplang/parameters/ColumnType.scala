/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Witold Jedrzejewski
 */

package io.deepsense.deeplang.parameters

/**
 * Types of data that column in dataframe can have.
 */
object ColumnType extends Enumeration {
  type ColumnType = Value
  val numeric = Value("numeric")
  val ordinal = Value("ordinal")
  val boolean = Value("boolean")
  val categorical = Value("categorical")
  val string = Value("string")
  val timestamp = Value("timestamp")
}
