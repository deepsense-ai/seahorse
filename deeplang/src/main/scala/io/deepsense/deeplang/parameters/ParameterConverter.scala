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
    def convertPF: PartialFunction[Any, StringParameter] = {
      case p: StringParameter => p
    }
  }

  implicit object ToNumericParameter extends ParameterConverter[NumericParameter] {
    def convertPF: PartialFunction[Any, NumericParameter] = {
      case p: NumericParameter => p
    }
  }

  implicit object ToBooleanParameter extends ParameterConverter[BooleanParameter] {
    def convertPF: PartialFunction[Any, BooleanParameter] = {
      case p: BooleanParameter => p
    }
  }

  implicit object ToChoiceParameter extends ParameterConverter[ChoiceParameter] {
    def convertPF: PartialFunction[Any, ChoiceParameter] = {
      case p: ChoiceParameter => p
    }
  }

  implicit object ToMultipleChoiceParameter
    extends ParameterConverter[MultipleChoiceParameter] {

    def convertPF: PartialFunction[Any, MultipleChoiceParameter] = {
      case p: MultipleChoiceParameter => p
    }
  }

  implicit object ToMultiplicatedParameter
    extends ParameterConverter[ParametersSequence] {

    def convertPF: PartialFunction[Any, ParametersSequence] = {
      case p: ParametersSequence => p
    }
  }

  implicit object ToSingleColumnSelectorParameter
    extends ParameterConverter[SingleColumnSelectorParameter] {

    def convertPF: PartialFunction[Any, SingleColumnSelectorParameter] = {
      case p: SingleColumnSelectorParameter => p
    }
  }

  implicit object ToColumnSelectionParameter
    extends ParameterConverter[ColumnSelectorParameter] {

    def convertPF: PartialFunction[Any, ColumnSelectorParameter] = {
      case p: ColumnSelectorParameter => p
    }
  }

  implicit object ToSingleColumnCreatorParameter
    extends ParameterConverter[SingleColumnCreatorParameter] {

    def convertPF: PartialFunction[Any, SingleColumnCreatorParameter] = {
      case p: SingleColumnCreatorParameter => p
    }
  }

  implicit object ToMultipleColumnCreatorParameter
    extends ParameterConverter[MultipleColumnCreatorParameter] {

    def convertPF: PartialFunction[Any, MultipleColumnCreatorParameter] = {
      case p: MultipleColumnCreatorParameter => p
    }
  }

  implicit object ToPrefixBasedColumnCreatorParameter
    extends ParameterConverter[PrefixBasedColumnCreatorParameter] {

    def convertPF: PartialFunction[Any, PrefixBasedColumnCreatorParameter] = {
      case p: PrefixBasedColumnCreatorParameter => p
    }
  }
}
