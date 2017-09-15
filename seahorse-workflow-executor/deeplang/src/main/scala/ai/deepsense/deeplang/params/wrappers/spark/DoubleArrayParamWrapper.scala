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

import ai.deepsense.deeplang.params.MultipleNumericParam
import ai.deepsense.deeplang.params.validators.{ArrayLengthValidator, ComplexArrayValidator, RangeValidator, Validator}

class DoubleArrayParamWrapper[P <: ml.param.Params](
    override val name: String,
    override val description: Option[String],
    val sparkParamGetter: P => ml.param.DoubleArrayParam,
    override val validator: Validator[Array[Double]] =
      ComplexArrayValidator(
        rangeValidator = RangeValidator.all,
        lengthValidator = ArrayLengthValidator.withAtLeast(1)))
  extends MultipleNumericParam(name, description, validator)
  with ForwardSparkParamWrapper[P, Array[Double]] {

  override def replicate(name: String): DoubleArrayParamWrapper[P] =
    new DoubleArrayParamWrapper[P](name, description, sparkParamGetter, validator)
}
