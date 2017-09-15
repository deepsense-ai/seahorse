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

import org.apache.spark.ml.feature.{Word2Vec => SparkWord2Vec, Word2VecModel => SparkWord2VecModel}

import ai.deepsense.deeplang.doperables.SparkSingleColumnEstimatorWrapper
import ai.deepsense.deeplang.doperables.spark.wrappers.models.Word2VecModel
import ai.deepsense.deeplang.doperables.spark.wrappers.params.Word2VecParams
import ai.deepsense.deeplang.params.Param

class Word2VecEstimator
  extends SparkSingleColumnEstimatorWrapper[SparkWord2VecModel, SparkWord2Vec, Word2VecModel]
  with Word2VecParams {

  override lazy val stepSizeDefault = 0.025
  override lazy val maxIterationsDefault = 1.0

  override protected def getSpecificParams: Array[Param[_]] = Array(
    maxIterations,
    stepSize,
    seed,
    vectorSize,
    numPartitions,
    minCount)
}
