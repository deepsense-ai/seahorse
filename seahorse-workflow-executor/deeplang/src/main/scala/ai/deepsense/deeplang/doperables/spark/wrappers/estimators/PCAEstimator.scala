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

package ai.deepsense.deeplang.doperables.spark.wrappers.estimators

import org.apache.spark.ml.feature.{PCA => SparkPCA, PCAModel => SparkPCAModel}

import ai.deepsense.deeplang.doperables.SparkSingleColumnEstimatorWrapper
import ai.deepsense.deeplang.doperables.spark.wrappers.models.PCAModel
import ai.deepsense.deeplang.params.Param
import ai.deepsense.deeplang.params.validators.RangeValidator
import ai.deepsense.deeplang.params.wrappers.spark._

class PCAEstimator
  extends SparkSingleColumnEstimatorWrapper[
    SparkPCAModel,
    SparkPCA,
    PCAModel] {

  val k = new IntParamWrapper[SparkPCA](
    name = "k",
    description = Some("The number of principal components."),
    sparkParamGetter = _.k,
    validator = RangeValidator(begin = 1.0, end = Int.MaxValue, step = Some(1.0)))
  setDefault(k, 1.0)

  override protected def getSpecificParams: Array[Param[_]] = Array(k)

  def setK(value: Int): this.type = {
    set(k -> value)
  }
}
