/**
 * Copyright 2015 deepsense.ai (CodiLime, Inc)
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

import ai.deepsense.deeplang.params.NumericParam
import ai.deepsense.deeplang.params.validators.{RangeValidator, Validator}

class IntParamWrapper[P <: ml.param.Params](
    override val name: String,
    override val description: Option[String],
    val sparkParamGetter: P => ml.param.IntParam,
    override val validator: Validator[Double] =
      RangeValidator(Int.MinValue, Int.MaxValue, step = Some(1.0)))
  extends NumericParam(name, description, validator)
  with SparkParamWrapper[P, Int, Double] {

  import IntParamWrapper._

  require(validatorHasMinMaxInIntegerRange(validator))
  require(validatorHasIntegerStep(validator))

  override def convert(value: Double)(schema: StructType): Int = value.toInt

  override def replicate(name: String): IntParamWrapper[P] =
    new IntParamWrapper[P](name, description, sparkParamGetter, validator)
}

object IntParamWrapper {

  def validatorHasMinMaxInIntegerRange(validator: Validator[Double]): Boolean = {
    validator match {
      case (v: RangeValidator) => inIntegerRange(v.begin) && inIntegerRange(v.end)
      case _ => true
    }
  }

  private def inIntegerRange(value: Double): Boolean =
    Int.MinValue <= value && value <= Int.MaxValue

  def validatorHasIntegerStep(validator: Validator[Double]): Boolean = {
    validator match {
      case (v: RangeValidator) => v.step match {
        case Some(step) => isInteger(step) && step > 0.0
        case None => false
      }
      case _ => true
    }
  }

  private def isInteger(v: Double): Boolean = Math.round(v) == v
}
