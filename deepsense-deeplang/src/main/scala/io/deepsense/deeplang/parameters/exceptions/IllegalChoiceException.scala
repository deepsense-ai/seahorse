/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Radoslaw Kotowski
 */

package io.deepsense.deeplang.parameters.exceptions

case class IllegalChoiceException(choice: String)
  extends ValidationException(s"Illegal choice parameter value: $choice.")
