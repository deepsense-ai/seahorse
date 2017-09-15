/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Witold Jedrzejewski
 */

package io.deepsense.deeplang.parameters

/**
 * Kinds of roles that column can assume in dataframe.
 */
object ColumnRole extends Enumeration {
  type ColumnRole = Value
  val feature = Value("feature")
  val label = Value("label")
  val prediction = Value("prediction")
  val identifier = Value("identifier")
  val ignored = Value("ignored")
}
