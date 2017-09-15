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

import ai.deepsense.deeplang.doperables.spark.wrappers.params.common.{ClassificationImpurity, FeatureSubsetStrategy}
import ai.deepsense.deeplang.params.ParamPair
import ai.deepsense.deeplang.params.selections.NameSingleColumnSelection

class RandomForestClassifierSmokeTest
  extends AbstractEstimatorModelWrapperSmokeTest {

  override def className: String = "RandomForestClassifier"

  override val estimator = new RandomForestClassifier()

  import estimator.vanillaRandomForestClassifier._

  override val estimatorParams: Seq[ParamPair[_]] = Seq(
    maxDepth -> 3,
    maxBins -> 40,
    impurity -> ClassificationImpurity.Entropy(),
    featuresColumn -> NameSingleColumnSelection("myFeatures"),
    labelColumn -> NameSingleColumnSelection("myLabel"),
    minInstancesPerNode -> 1,
    minInfoGain -> 2,
    maxMemoryInMB -> 20,
    cacheNodeIds -> true,
    checkpointInterval -> 3,
    subsamplingRate -> 0.5,
    seed -> 555,
    numTrees -> 30,
    featureSubsetStrategy -> FeatureSubsetStrategy.Auto()
  )
}
