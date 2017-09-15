/**
 * Copyright 2016, deepsense.io
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

package io.deepsense.deeplang.doperables.spark.wrappers.models

import org.apache.spark.ml.regression.{DecisionTreeRegressionModel => SparkDecisionTreeRegressionModel, DecisionTreeRegressor => SparkDecisionTreeRegressor}

import io.deepsense.deeplang.ExecutionContext
import io.deepsense.deeplang.doperables.SparkModelWrapper
import io.deepsense.deeplang.doperables.serialization.SerializableSparkModel
import io.deepsense.deeplang.doperables.spark.wrappers.params.common.{HasFeaturesColumnParam, HasPredictionColumnCreatorParam}
import io.deepsense.deeplang.params.Param

class DecisionTreeRegressionModel
  extends SparkModelWrapper[
    SparkDecisionTreeRegressionModel,
    SparkDecisionTreeRegressor]
  with HasFeaturesColumnParam
  with HasPredictionColumnCreatorParam {

  override val params: Array[Param[_]] = declareParams(
    featuresColumn,
    predictionColumn)

  override protected def loadModel(
      ctx: ExecutionContext,
      path: String): SerializableSparkModel[SparkDecisionTreeRegressionModel] = {
    new SerializableSparkModel(SparkDecisionTreeRegressionModel.load(path))
  }
}
