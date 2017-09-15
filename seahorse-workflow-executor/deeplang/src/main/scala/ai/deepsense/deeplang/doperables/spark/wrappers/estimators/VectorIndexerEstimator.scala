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

import org.apache.spark.ml.feature.{VectorIndexer => SparkVectorIndexer, VectorIndexerModel => SparkVectorIndexerModel}

import ai.deepsense.deeplang.doperables.SparkSingleColumnEstimatorWrapper
import ai.deepsense.deeplang.doperables.spark.wrappers.models.VectorIndexerModel
import ai.deepsense.deeplang.params.Param
import ai.deepsense.deeplang.params.validators.RangeValidator
import ai.deepsense.deeplang.params.wrappers.spark.IntParamWrapper

class VectorIndexerEstimator
  extends SparkSingleColumnEstimatorWrapper[
    SparkVectorIndexerModel,
    SparkVectorIndexer,
    VectorIndexerModel] {

  val maxCategories = new IntParamWrapper[SparkVectorIndexer](
    name = "max categories",
    description =
      Some("""The threshold for the number of values a categorical feature can take.
        |If a feature is found to have more values, then it is declared continuous.""".stripMargin),
    sparkParamGetter = _.maxCategories,
    validator = RangeValidator(begin = 2.0, end = Int.MaxValue, step = Some(1.0)))
  setDefault(maxCategories, 20.0)

  override protected def getSpecificParams: Array[Param[_]] = Array(maxCategories)

  def setMaxCategories(value: Int): this.type = {
    set(maxCategories -> value)
  }
}
