/**
 * Copyright 2015, deepsense.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.deepsense.deeplang.parameters

import scala.util.matching.Regex

import spray.json.JsObject

import io.deepsense.deeplang.exceptions.DeepLangException
import io.deepsense.deeplang.parameters.exceptions.{MatchException, OutOfRangeException, OutOfRangeWithStepException}

/**
 * Validates if NumericParameter value is within range bounds.
 */
case class RangeValidator(
    begin: Double,
    end: Double,
    beginIncluded: Boolean = true,
    endIncluded: Boolean = true,
    step: Option[Double] = None)
  extends Validator[Double] {

  import RangeValidator.Epsilon

  require(begin <= end)
  step.foreach(s => require(s > 0))
  step.foreach(s => require(
    math.abs(takeSteps(countStepsTo(end, s), s) - end) < Epsilon,
    "Length of range should be divisible by step."))

  val validatorType = ValidatorType.Range

  override def validate(parameter: Double): Vector[DeepLangException] = {
    val beginComparison: (Double, Double) => Boolean = if (beginIncluded) (_ >= _) else (_ > _)
    val endComparison: (Double, Double) => Boolean = if (endIncluded) (_ <= _) else (_ < _)
    if (!(beginComparison(parameter, begin) && endComparison(parameter, end))) {
      Vector(new OutOfRangeException(parameter, begin, end))
    } else {
      validateStep(parameter)
    }
  }

  /** Validates if parameter value can be reached using given step */
  private def validateStep(value: Double): Vector[DeepLangException] = {
    step.foreach {
      s => if (math.abs(takeSteps(countStepsTo(value, s), s) - value) >= Epsilon) {
        return Vector(OutOfRangeWithStepException(value, begin, end, s))
      }
    }
    Vector.empty
  }

  /**
   * Counts number of steps that needs to be taken to get to the given value.
   * If number of steps is not an integer then the floor of that number is returned.
   */
  private def countStepsTo(value: Double, step: Double): Long =
    ((value - begin) / step).floor.toLong

  /** Computes the value after given number of steps starting at `begin` of range. */
  private def takeSteps(count: Long, step: Double): Double = begin + step * count

  override def configurationToJson: JsObject = {
    import ValidatorsJsonProtocol.rangeValidatorFormat
    rangeValidatorFormat.write(this).asJsObject
  }
}

object RangeValidator {
  val Epsilon = 1e-10
}

/**
 * Validates if StringParameter value matches the given regex.
 */
case class RegexValidator(
    regex: Regex)
  extends Validator[String] {

  val validatorType = ValidatorType.Regex

  override def validate(parameter: String): Vector[DeepLangException] = {
    if (parameter matches regex.toString) {
      Vector.empty
    } else {
      Vector(MatchException(parameter, regex))
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

/**
 * Validator which accepts a single character.
 */
class SingleCharRegexValidator() extends RegexValidator(".".r)
