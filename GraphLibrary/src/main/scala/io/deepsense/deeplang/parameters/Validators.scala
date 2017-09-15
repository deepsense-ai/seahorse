/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Radoslaw Kotowski
 */

package io.deepsense.deeplang.parameters

import io.deepsense.deeplang.parameters.exceptions.{OutOfRangeWithStepException, OutOfRangeException}

/**
 * Validates if NumericParameter value is within range bounds.
 * TODO: take into account an epsilon when comparing floating point numbers.
 */
case class RangeValidator(
    begin: Double,
    end: Double,
    beginIncluded: Boolean = true,
    endIncluded: Boolean = true,
    step: Option[Double] = None) extends Validator[NumericParameter] {
  require(begin <= end)
  step.foreach(s => require(makeSteps(countStepsTo(end, s), s) == end,
    "Length of range should be divisible by step."))

  val validatorType = ValidatorType.Range

  override def validate(parameter: NumericParameter): Unit = {
    val beginComparison: (Double, Double) => Boolean = if (beginIncluded) (_ >= _) else (_ > _)
    val endComparison: (Double, Double) => Boolean = if (endIncluded) (_ <= _) else (_ < _)
    if (!(beginComparison(parameter.value, begin) && endComparison(parameter.value, end))) {
      throw new OutOfRangeException(parameter.value, begin, end)
    } else {
      validateStep(parameter.value)
    }
  }

  /** Validates if parameter value can be reached using given step */
  private def validateStep(value: Double): Unit = {
    step.foreach {
      s => if (s <= 0 || makeSteps(countStepsTo(value, s), s) != value) {
        throw OutOfRangeWithStepException(value, begin, end, s)
      }
    }
  }

  /**
   * Counts number of steps that needs to be taken to get to the given value.
   * If number of steps is not an integer then the floor of that number is returned.
   */
  private def countStepsTo(value: Double, step: Double): Int = ((value - begin)/step).floor.toInt

  /** Computes the value after given number of steps starting at `begin` of range. */
  private def makeSteps(count: Int, step: Double): Double = begin + step * count
}
