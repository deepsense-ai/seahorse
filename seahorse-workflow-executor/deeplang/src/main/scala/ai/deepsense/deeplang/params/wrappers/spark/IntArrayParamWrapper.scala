/**
 * Copyright 2016 deepsense.ai (CodiLime, Inc)
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

package ai.deepsense.deeplang.params.wrappers.spark

import org.apache.spark.ml
import org.apache.spark.sql.types.StructType

import ai.deepsense.deeplang.params.MultipleNumericParam
import ai.deepsense.deeplang.params.validators.{ComplexArrayValidator, RangeValidator, Validator}

class IntArrayParamWrapper[P <: ml.param.Params](
    override val name: String,
    override val description: Option[String],
    val sparkParamGetter: P => ml.param.IntArrayParam,
    override val validator: Validator[Array[Double]] =
      ComplexArrayValidator(
        RangeValidator(Int.MinValue, Int.MaxValue, step = Some(1.0))))
  extends MultipleNumericParam(name, description, validator)
  with SparkParamWrapper[P, Array[Int], Array[Double]] {

  import IntArrayParamWrapper._

  require(arrayValidatorHasMinMaxInIntegerRange(validator))
  require(arrayValidatorHasIntegerStep(validator))

  override def convert(values: Array[Double])(schema: StructType): Array[Int] =
    values.map(value => value.toInt)

  override def replicate(name: String): IntArrayParamWrapper[P] =
    new IntArrayParamWrapper[P](name, description, sparkParamGetter, validator)
}

object IntArrayParamWrapper {

  def arrayValidatorHasMinMaxInIntegerRange(validator: Validator[Array[Double]]): Boolean = {
    import IntParamWrapper.validatorHasMinMaxInIntegerRange

    validator match {
      case ComplexArrayValidator(rangeValidator, arrayLengthValidator) =>
        validatorHasMinMaxInIntegerRange(rangeValidator)
    }
  }

  def arrayValidatorHasIntegerStep(validator: Validator[Array[Double]]): Boolean = {
    import IntParamWrapper.validatorHasIntegerStep

    validator match {
    case ComplexArrayValidator(rangeValidator, arrayLengthValidator) =>
      validatorHasIntegerStep(rangeValidator)
    }
  }
}
