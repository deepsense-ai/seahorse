/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Witold Jedrzejewski
 */

package io.deepsense.deeplang.parameters

object ValidatorType extends Enumeration {
  type ValidatorType = Value
  val Range = Value("range")
  val Regex = Value("regex")
}
