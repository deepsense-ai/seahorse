/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Radoslaw Kotowski
 */

package io.deepsense.deeplang.parameters

import scala.reflect.runtime.{universe => ru}

import io.deepsense.deeplang.parameters.exceptions.TypeConversionException

/** Parameters converter used for implicit conversions */
abstract class ParameterConverter[T : ru.TypeTag] {
  /** Returns a function converting a parameter into `T`. */
  def convertPF: PartialFunction[Any, T]

  /** Converts and object or throws TypeConversionException if the object can't be converted. */
  def convert(parameter: Option[Any]): Option[T] = {
    parameter match {
      case Some(p) => Some(convertPF.applyOrElse(p, (_: Any) =>
        throw TypeConversionException(p, ru.typeOf[T].typeSymbol.fullName)))
      case None => None
    }
  }
}

/**
 * Object containing implicit conversions of Parameters to target types.
 * These conversions are used to get some specific parameter.
 */
object ParameterConversions {
  implicit object ToString extends ParameterConverter[String] {
    def convertPF = {
      case parameter: String => parameter
    }
  }

  implicit object ToDouble extends ParameterConverter[Double] {
    def convertPF = {
      case parameter: Double => parameter
    }
  }

  implicit object ToBoolean extends ParameterConverter[Boolean] {
    def convertPF = {
      case parameter: Boolean => parameter
    }
  }

  implicit object ToChoiceParameter extends ParameterConverter[ChoiceParameter] {
    def convertPF = {
      case parameter: ChoiceParameter => parameter
    }
  }

  implicit object ToMultipleChoiceParameter extends ParameterConverter[MultipleChoiceParameter] {
    def convertPF = {
      case parameter: MultipleChoiceParameter => parameter
    }
  }

  implicit object ToMultiplicatorParameter extends ParameterConverter[MultiplicatorParameter] {
    def convertPF = {
      case parameter: MultiplicatorParameter => parameter
    }
  }

  implicit object ToSingleColumnSelection
    extends ParameterConverter[SingleColumnSelection] {
    def convertPF = {
      case parameter: SingleColumnSelection => parameter
    }
  }

  implicit object ToColumnSelection
      extends ParameterConverter[MultipleColumnSelection] {
    def convertPF = {
      case parameter: MultipleColumnSelection => parameter
    }
  }
}
