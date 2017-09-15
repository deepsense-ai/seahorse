/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Witold Jedrzejewski
 */

package io.deepsense.deeplang.parameters.exceptions

case class IllegalChoiceException(choice: String)
  extends ValidationException(s"Illegal choice parameter value: $choice.")
