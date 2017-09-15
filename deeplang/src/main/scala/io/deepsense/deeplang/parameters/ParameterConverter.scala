/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Witold Jedrzejewski
 */

package io.deepsense.deeplang.parameters

import scala.reflect.runtime.{universe => ru}

import io.deepsense.deeplang.parameters.exceptions.TypeConversionException

/** Parameters converter used for implicit conversions */
abstract class ParameterConverter[T <: Parameter : ru.TypeTag] {
  /** Returns a function converting a parameter into `T`. */
  def convertPF: PartialFunction[Any, T]

  /** Converts and object or throws TypeConversionException if the object can't be converted. */
  def convert(p: Any): T = {
    convertPF.applyOrElse(p, (_: Any) =>
      throw TypeConversionException(p, ru.typeOf[T].typeSymbol.fullName)
    )
  }
}

/**
 * Object containing implicit conversions of Parameters to target types.
 * These conversions are used to get some specific parameter.
 */
object ParameterConversions {
  implicit object ToStringParameter extends ParameterConverter[StringParameter] {
    def convertPF = {
      case p: StringParameter => p
    }
  }

  implicit object ToNumericParameter extends ParameterConverter[NumericParameter] {
    def convertPF = {
      case p: NumericParameter => p
    }
  }

  implicit object ToBooleanParameter extends ParameterConverter[BooleanParameter] {
    def convertPF = {
      case p: BooleanParameter => p
    }
  }

  implicit object ToChoiceParameter extends ParameterConverter[ChoiceParameter] {
    def convertPF = {
      case p: ChoiceParameter => p
    }
  }

  implicit object ToMultipleChoiceParameter
    extends ParameterConverter[MultipleChoiceParameter] {

    def convertPF = {
      case p: MultipleChoiceParameter => p
    }
  }

  implicit object ToMultiplicatedParameter
    extends ParameterConverter[ParametersSequence] {

    def convertPF = {
      case p: ParametersSequence => p
    }
  }

  implicit object ToSingleColumnSelectorParameter
    extends ParameterConverter[SingleColumnSelectorParameter] {
    def convertPF = {
      case p: SingleColumnSelectorParameter => p
    }
  }

  implicit object ToColumnSelectionParameter
      extends ParameterConverter[ColumnSelectorParameter] {
    def convertPF = {
      case p: ColumnSelectorParameter => p
    }
  }
}
