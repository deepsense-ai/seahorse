/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Radoslaw Kotowski
 */

package io.deepsense.deeplang.parameters

import io.deepsense.deeplang.parameters.exceptions.IllegalChoiceException

/**
 * Represents ParameterHolder with possible choices.
 */
trait HasChoice extends ParameterHolder {
  val options: Map[String, ParametersSchema]

  /**
   * Validates if chosen values schemas exist within possible choices.
   * If they exist then the choice values (namely their schemas) are validated.
   */
  def validateChoices(chosen: Set[ChoiceParameter]) = {
    val illegal = chosen.find(ch => !options.contains(ch.label))
    illegal match {
      case Some(ChoiceParameter(label, value)) => throw IllegalChoiceException(label)
      case None => chosen.foreach(_.value.validate)
    }
  }
}
