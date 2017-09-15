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

package ai.deepsense.deeplang.doperables.spark.wrappers.models

import org.apache.spark.ml.classification.{DecisionTreeClassificationModel => SparkDecisionTreeClassificationModel, DecisionTreeClassifier => SparkDecisionTreeClassifier}

import ai.deepsense.deeplang.doperables.spark.wrappers.params.common.ProbabilisticClassifierParams
import ai.deepsense.deeplang.doperables.stringindexingwrapper.StringIndexingWrapperModel
import ai.deepsense.deeplang.doperables.{LoadableWithFallback, SparkModelWrapper}
import ai.deepsense.deeplang.params.Param
import ai.deepsense.sparkutils.ML

class DecisionTreeClassificationModel(
    vanillaModel: VanillaDecisionTreeClassificationModel)
  extends StringIndexingWrapperModel[
    SparkDecisionTreeClassificationModel,
    SparkDecisionTreeClassifier](vanillaModel) {

  def this() = this(new VanillaDecisionTreeClassificationModel())
}

class VanillaDecisionTreeClassificationModel
  extends SparkModelWrapper[
    SparkDecisionTreeClassificationModel,
    SparkDecisionTreeClassifier]
  with ProbabilisticClassifierParams
  with LoadableWithFallback[
    SparkDecisionTreeClassificationModel,
    SparkDecisionTreeClassifier] {

  override val params: Array[Param[_]] = Array(
    featuresColumn,
    probabilityColumn,
    rawPredictionColumn,
    predictionColumn)

  override protected def transformerName: String =
    classOf[DecisionTreeClassificationModel].getSimpleName

  override def tryToLoadModel(path: String): Option[SparkDecisionTreeClassificationModel] = {
    ML.ModelLoading.decisionTreeClassification(path)
  }
}
