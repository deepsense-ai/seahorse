/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Witold Jedrzejewski
 */

package io.deepsense.deeplang.parameters.exceptions

import scala.util.matching.Regex

case class MatchException(value: String, regex: Regex)
  extends ValidationException(s"Parameter value `$value` does not match regex `$regex`.")
