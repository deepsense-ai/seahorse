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

package ai.deepsense.deeplang.params

import spray.json.DefaultJsonProtocol.DoubleJsonFormat

import ai.deepsense.deeplang.params.validators.{RangeValidator, Validator}

case class NumericParam(
    override val name: String,
    override val description: Option[String],
    validator: Validator[Double] = RangeValidator.all)
  extends ParamWithJsFormat[Double]
  with HasValidator[Double] {

  override val parameterType = ParameterType.Numeric

  override val isGriddable: Boolean = true

  override def replicate(name: String): NumericParam = copy(name = name)
}
