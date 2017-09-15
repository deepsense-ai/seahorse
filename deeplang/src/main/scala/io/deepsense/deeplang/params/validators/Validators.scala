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

package io.deepsense.deeplang.params.validators

import scala.util.matching.Regex

import spray.json.JsObject

import io.deepsense.deeplang.exceptions.DeepLangException
import io.deepsense.deeplang.params.exceptions.{OutOfRangeWithStepException, OutOfRangeException, MatchException}

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

  override def validate(name: String, parameter: Double): Vector[DeepLangException] = {
    val beginComparison: (Double, Double) => Boolean = if (beginIncluded) (_ >= _) else (_ > _)
    val endComparison: (Double, Double) => Boolean = if (endIncluded) (_ <= _) else (_ < _)
    if (!(beginComparison(parameter, begin) && endComparison(parameter, end))) {
      Vector(new OutOfRangeException(name, parameter, begin, end))
    } else {
      validateStep(name, parameter)
    }
  }

  /** Validates if parameter value can be reached using given step */
  private def validateStep(name: String, value: Double): Vector[DeepLangException] = {
    step.foreach {
      s => if (math.abs(takeSteps(countStepsTo(value, s), s) - value) >= Epsilon) {
        return Vector(OutOfRangeWithStepException(name, value, begin, end, s))
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

  /**
    * Generates human-readable range constraints to be added as a description.
    * Ex. 1: RangeValidator(0.0, Int.MaxValue, true, false, Some(1))).toHumanReadable("num param"):
    *  Range constraints: 0 <= `num param` and `num param` is an integer.
    *
    * Ex. 2: RangeValidator(0.0, 3.0, false, true, Some(0.5))).toHumanReadable("num param"):
    *  Range constraints: 0 < `num param` <= 3 and `num param` = k*0.5, where k is an integer.
    */
  override def toHumanReadable(paramName: String): String = {
    val beginConstraint = Begin()
    val endConstraint = End()
    val (isIntStep, mappedStep) = step match {
      case Some(s) =>
        val isInt = s == s.round
        (isInt, Some(if (isInt) s.round.toString else s.toString))
      case None => (false, None)
    }
    val isIntRange = beginConstraint.mapsToInt && endConstraint.mapsToInt && isIntStep
    val rangeDescription = {
      if (beginConstraint.isOneOfLimits && endConstraint.isOneOfLimits) {
        ""
      } else {
        beginConstraint.create + s"`$paramName`" + endConstraint.create + " and "
      }
    }
    val stepDescription = {
      def beginSum: String = {
        val strBegin = beginConstraint.mappedLimit
        if (strBegin == "0") "" else strBegin + " + "
      }
      (isIntRange, mappedStep) match {
        case (true, Some("1")) => s"`$paramName` is an integer."
        case (_, Some(s)) => s"`$paramName` = ${beginSum}k*$s, where k is an integer."
        case (_, None) => s"`$paramName` is a floating point number."
      }
    }
    " Range constraints: " + rangeDescription + stepDescription
  }

  abstract class Constraint(limit: Double) {
    val limits: List[Double]
    def buildConstraint(oneOfLimits: Boolean, limitRepresentation: String): Option[String]

    lazy val isOneOfLimits = limits.contains(limit)
    val mapsToInt = limit.round == limit
    val mappedLimit = if (mapsToInt) limit.round.toString else limit.toString

    def create: String = buildConstraint(isOneOfLimits, mappedLimit).getOrElse("")
  }

  case class Begin() extends Constraint(begin) {
    val included = beginIncluded
    override val limits = List(
      Int.MinValue.toDouble,
      Long.MinValue.toDouble,
      Float.MinValue.toDouble,
      Double.MinValue,
      Double.NegativeInfinity
    )
    override def buildConstraint(oneOfLimits: Boolean, limitRepresentation: String)
        : Option[String] = {
      if (oneOfLimits) {
        None
      } else {
        Some(limitRepresentation.concat(if (included) " <= " else " < "))
      }
    }
  }

  case class End() extends Constraint(end) {
    val included = endIncluded
    override val limits = List(
      Int.MaxValue.toDouble,
      Long.MaxValue.toDouble,
      Float.MaxValue.toDouble,
      Double.MaxValue,
      Double.PositiveInfinity
    )
    override def buildConstraint(oneOfLimits: Boolean, limitRepresentation: String)
        : Option[String] = {
      if (oneOfLimits) {
        None
      } else {
        Some((if (included) " <= " else " < ").concat(limitRepresentation))
      }
    }
  }

}

object RangeValidator {
  val Epsilon = 1e-10

  def all: RangeValidator = RangeValidator(begin = Double.MinValue, end = Double.MaxValue)

  def positiveIntegers: RangeValidator =
    RangeValidator(
      begin = 0.0,
      end = Int.MaxValue,
      beginIncluded = true,
      endIncluded = true,
      step = Some(1.0))
}

/**
 * Validates if StringParameter value matches the given regex.
 */
case class RegexValidator(
    regex: Regex)
  extends Validator[String] {

  val validatorType = ValidatorType.Regex

  override def validate(name: String, parameter: String): Vector[DeepLangException] = {
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
