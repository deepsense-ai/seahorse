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

import ai.deepsense.deeplang.doperables.spark.wrappers.params.common.ClassificationImpurity.Gini
import ai.deepsense.deeplang.params.ParamPair
import ai.deepsense.deeplang.params.selections.NameSingleColumnSelection

class DecisionTreeClassifierSmokeTest extends AbstractEstimatorModelWrapperSmokeTest {

  override def className: String = "DecisionTreeClassifier"

  override val estimator = new DecisionTreeClassifier()

  import estimator.vanillaDecisionTreeClassifier._

  override val estimatorParams: Seq[ParamPair[_]] = Seq(
    maxDepth -> 6.0,
    maxBins -> 28.0,
    minInstancesPerNode -> 2.0,
    minInfoGain -> 0.05,
    maxMemoryInMB -> 312.0,
    cacheNodeIds -> false,
    checkpointInterval -> 8.0,
    seed -> 12345.0,
    impurity -> Gini(),
    featuresColumn -> NameSingleColumnSelection("myFeatures"),
    labelColumn -> NameSingleColumnSelection("myLabel"),
    probabilityColumn -> "prob",
    rawPredictionColumn -> "rawPred",
    predictionColumn -> "pred"
  )
}
