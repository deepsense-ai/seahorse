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

package ai.deepsense.deeplang.doperables.spark.wrappers.estimators

import org.apache.spark.ml.classification.{DecisionTreeClassificationModel => SparkDecisionTreeClassificationModel, DecisionTreeClassifier => SparkDecisionTreeClassifier}

import ai.deepsense.deeplang.doperables.SparkEstimatorWrapper
import ai.deepsense.deeplang.doperables.spark.wrappers.models.{DecisionTreeClassificationModel, VanillaDecisionTreeClassificationModel}
import ai.deepsense.deeplang.doperables.spark.wrappers.params.DecisionTreeParams
import ai.deepsense.deeplang.doperables.spark.wrappers.params.common._
import ai.deepsense.deeplang.doperables.stringindexingwrapper.StringIndexingEstimatorWrapper
import ai.deepsense.deeplang.params.Param

class DecisionTreeClassifier private(
    val vanillaDecisionTreeClassifier: VanillaDecisionTreeClassifier)
  extends StringIndexingEstimatorWrapper[
    SparkDecisionTreeClassificationModel,
    SparkDecisionTreeClassifier,
    VanillaDecisionTreeClassificationModel,
    DecisionTreeClassificationModel](vanillaDecisionTreeClassifier) {

  def this() = this(new VanillaDecisionTreeClassifier())
}

class VanillaDecisionTreeClassifier
  extends SparkEstimatorWrapper[
    SparkDecisionTreeClassificationModel,
    SparkDecisionTreeClassifier,
    VanillaDecisionTreeClassificationModel]
  with HasClassificationImpurityParam
  with DecisionTreeParams
  with ProbabilisticClassifierParams
  with HasLabelColumnParam {

  override val params: Array[Param[_]] = Array(
    maxDepth,
    maxBins,
    minInstancesPerNode,
    minInfoGain,
    maxMemoryInMB,
    cacheNodeIds,
    checkpointInterval,
    seed,
    impurity,
    labelColumn,
    featuresColumn,
    probabilityColumn,
    rawPredictionColumn,
    predictionColumn)

  override protected def estimatorName: String = classOf[DecisionTreeClassifier].getSimpleName
}
