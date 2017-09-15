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

import org.apache.spark.ml.regression.{IsotonicRegression => SparkIsotonicRegression, IsotonicRegressionModel => SparkIsotonicRegressionModel}

import ai.deepsense.deeplang.doperables.SparkEstimatorWrapper
import ai.deepsense.deeplang.doperables.spark.wrappers.models.IsotonicRegressionModel
import ai.deepsense.deeplang.doperables.spark.wrappers.params.common._
import ai.deepsense.deeplang.params.wrappers.spark.BooleanParamWrapper

class IsotonicRegression
  extends SparkEstimatorWrapper[
    SparkIsotonicRegressionModel, SparkIsotonicRegression, IsotonicRegressionModel]
  with PredictorParams
  with HasFeatureIndexParam
  with HasLabelColumnParam
  with HasOptionalWeightColumnParam {

  val isotonic = new BooleanParamWrapper[SparkIsotonicRegression](
    name = "isotonic",
    description =
      Some("""Whether the output sequence should be isotonic/increasing (true)
        |or antitonic/decreasing (false).""".stripMargin),
    sparkParamGetter = _.isotonic)
  setDefault(isotonic, true)

  override val params: Array[ai.deepsense.deeplang.params.Param[_]] = Array(
    isotonic,
    optionalWeightColumn,
    featureIndex,
    labelColumn,
    featuresColumn,
    predictionColumn)
}
