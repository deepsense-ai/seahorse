/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.parameters

import scala.util.matching.Regex

import spray.json.JsObject

import io.deepsense.deeplang.parameters.exceptions.{MatchException, OutOfRangeException, OutOfRangeWithStepException}

/**
 * Validates if NumericParameter value is within range bounds.
 * TODO: take into account an epsilon when comparing floating point numbers.
 */
case class RangeValidator(
    begin: Double,
    end: Double,
    beginIncluded: Boolean = true,
    endIncluded: Boolean = true,
    step: Option[Double] = None)
  extends Validator[Double] {

  require(begin <= end)
  step.foreach(s => require(s > 0))
  step.foreach(s => require(takeSteps(countStepsTo(end, s), s) == end,
    "Length of range should be divisible by step."))

  val validatorType = ValidatorType.Range

  override def validate(parameter: Double): Unit = {
    val beginComparison: (Double, Double) => Boolean = if (beginIncluded) (_ >= _) else (_ > _)
    val endComparison: (Double, Double) => Boolean = if (endIncluded) (_ <= _) else (_ < _)
    if (!(beginComparison(parameter, begin) && endComparison(parameter, end))) {
      throw new OutOfRangeException(parameter, begin, end)
    } else {
      validateStep(parameter)
    }
  }

  /** Validates if parameter value can be reached using given step */
  private def validateStep(value: Double): Unit = {
    step.foreach {
      s => if (takeSteps(countStepsTo(value, s), s) != value) {
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
  private def takeSteps(count: Int, step: Double): Double = begin + step * count

  override def configurationToJson: JsObject = {
    import ValidatorsJsonProtocol.rangeValidatorFormat
    rangeValidatorFormat.write(this).asJsObject
  }
}

/**
 * Validates if StringParameter value matches the given regex.
 */
case class RegexValidator(
    regex: Regex)
  extends Validator[String] {

  val validatorType = ValidatorType.Regex

  override def validate(parameter: String): Unit = {
    if (!(parameter matches regex.toString)) {
      throw MatchException(parameter, regex)
    }
  }

  override def configurationToJson: JsObject = {
    import ValidatorsJsonProtocol.regexValidatorFormat
    regexValidatorFormat.write(this).asJsObject
  }
}

/**
 * Validator which accepts all strings.
 */
class AcceptAllRegexValidator() extends RegexValidator(".*".r)
