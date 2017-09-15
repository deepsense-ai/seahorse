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

import org.apache.spark.ml.classification.{DecisionTreeClassificationModel => SparkDecisionTreeClassificationModel, DecisionTreeClassifier => SparkDecisionTreeClassifier}

import io.deepsense.deeplang.ExecutionContext
import io.deepsense.deeplang.doperables.{SparkModelWrapper, Transformer}
import io.deepsense.deeplang.doperables.serialization.{CustomPersistence, SerializableSparkModel}
import io.deepsense.deeplang.doperables.spark.wrappers.params.common.ProbabilisticClassifierParams
import io.deepsense.deeplang.doperables.stringindexingwrapper.StringIndexingWrapperModel
import io.deepsense.deeplang.params.Param

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
  with ProbabilisticClassifierParams {

  override val params: Array[Param[_]] = declareParams(
    featuresColumn,
    probabilityColumn,
    rawPredictionColumn,
    predictionColumn)

  override protected def loadModel(
      ctx: ExecutionContext,
      path: String): SerializableSparkModel[SparkDecisionTreeClassificationModel] = {
    val modelPath = Transformer.modelFilePath(path)
    CustomPersistence.load[SerializableSparkModel[SparkDecisionTreeClassificationModel]](
      ctx.sparkContext,
      modelPath)
  }

  override protected def transformerName: String =
    classOf[DecisionTreeClassificationModel].getSimpleName
}
