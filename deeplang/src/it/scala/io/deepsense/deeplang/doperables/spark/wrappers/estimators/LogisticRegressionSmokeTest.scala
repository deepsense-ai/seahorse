/**
 * Copyright 2015, deepsense.io
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

package io.deepsense.deeplang.doperables.spark.wrappers.estimators

import io.deepsense.deeplang.params.ParamPair
import io.deepsense.deeplang.params.selections.NameSingleColumnSelection
import org.apache.spark.ml.classification.{LogisticRegression => SparkLogisticRegression, LogisticRegressionModel => SparkLogisticRegressionModel}

class LogisticRegressionSmokeTest
  extends AbstractEstimatorModelWrapperSmokeTest[SparkLogisticRegression] {

  override def className: String = "LogisticRegression"

  override val estimatorWrapper = new LogisticRegression()

  override val estimatorParams: Seq[ParamPair[_]] = Seq(
    ParamPair(estimatorWrapper.elasticNetParameter, 0.8),
    ParamPair(estimatorWrapper.fitIntercept, true),
    ParamPair(estimatorWrapper.maxIterations, 2.0),
    ParamPair(estimatorWrapper.regularizationParameter, 0.1),
    ParamPair(estimatorWrapper.tolerance, 0.01),
    ParamPair(estimatorWrapper.standardization, true),
    ParamPair(estimatorWrapper.featuresColumn, NameSingleColumnSelection("myFeatures")),
    ParamPair(estimatorWrapper.labelColumn, NameSingleColumnSelection("myLabel")),
    ParamPair(estimatorWrapper.probabilityColumn, "prob"),
    ParamPair(estimatorWrapper.rawPredictionColumn, "rawPred"),
    ParamPair(estimatorWrapper.predictionColumn, "pred"),
    ParamPair(estimatorWrapper.threshold, 0.3)
  )
}
