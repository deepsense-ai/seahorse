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
  def convertPF: PartialFunction[Parameter, T]

  /** Converts and object or throws TypeConversionException if the object can't be converted. */
  def convert(parameter: Option[Parameter]): Option[T] = {
    parameter match {
      case Some(p) => Some(convertPF.applyOrElse(p, (_: Parameter) =>
        throw TypeConversionException(p, ru.typeOf[T].typeSymbol.fullName)))
      case None => None
    }
  }
}

/**
 * Object containing implicit conversions of Parameters to target types.
 * These conversions are used when DOperation wants to get some specific parameter.
 */
object ParameterConversions {
  implicit object ToStringParameter extends ParameterConverter[StringParameter] {
    def convertPF = {
      case parameter: StringParameter => parameter
    }
  }

  implicit object ToNumericParameter extends ParameterConverter[NumericParameter] {
    def convertPF = {
      case parameter: NumericParameter => parameter
    }
  }

  implicit object ToBooleanParameter extends ParameterConverter[BooleanParameter] {
    def convertPF = {
      case parameter: BooleanParameter => parameter
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
}
