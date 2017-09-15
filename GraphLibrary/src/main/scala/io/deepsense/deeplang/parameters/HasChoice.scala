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
   * Validates that chosen values schemas exist within possible choices.
   * Assumes that option labels are set correctly.
   * @param chosen chosen values to validate
   */
  protected def validateChoices(chosen: Traversable[ChoiceParameter]) = {
    for (ChoiceParameter(label, value) <- chosen) {
      options(label).validate
    }
  }

  /**
   * Fills options map using provided fillers and returns obtained ChoiceParameters.
   * Fillers is map from label to filling function.
   * Each filling function has to be able to fill schema associated with its label.
   * If some label from fillers map does not exist in options, IllegalChoiceException is thrown.
   * @param fillers map from labels to filling functions
   * @return collection of ChoiceParameters obtained during filling process
   */
  protected def fillChosen(
      fillers: Map[String, ParametersSchema => Unit]): Traversable[ChoiceParameter] = {
    for ((label, filler) <- fillers) yield {
      options.get(label) match {
        case Some(selectedOption) =>
          filler(selectedOption)
          ChoiceParameter(label, selectedOption)
        case None => throw IllegalChoiceException(label)
      }
    }
  }
}
