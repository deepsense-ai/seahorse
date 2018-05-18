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

import org.apache.spark.ml.classification.{MultilayerPerceptronClassificationModel => SparkMultilayerPerceptronClassifierModel, MultilayerPerceptronClassifier => SparkMultilayerPerceptronClassifier}

import ai.deepsense.deeplang.doperables.report.CommonTablesGenerators.SparkSummaryEntry
import ai.deepsense.deeplang.doperables.report.{CommonTablesGenerators, Report}
import ai.deepsense.deeplang.doperables.spark.wrappers.params.common.PredictorParams
import ai.deepsense.deeplang.doperables.{LoadableWithFallback, SparkModelWrapper}
import ai.deepsense.deeplang.params.Param
import ai.deepsense.sparkutils.ML

class MultilayerPerceptronClassifierModel
  extends SparkModelWrapper[
    SparkMultilayerPerceptronClassifierModel,
    SparkMultilayerPerceptronClassifier]
  with LoadableWithFallback[
    SparkMultilayerPerceptronClassifierModel,
    SparkMultilayerPerceptronClassifier]
  with PredictorParams {

  override val params: Array[Param[_]] = Array(
    featuresColumn,
    predictionColumn)

  override def report(extended: Boolean = true): Report = {
    val numberOfFeatures =
      SparkSummaryEntry(
        name = "number of features",
        value = sparkModel.numFeatures,
        description = "Number of features.")

    val layers =
      SparkSummaryEntry(
        name = "layers",
        value = sparkModel.layers,
        description =
          """The list of layer sizes that includes the input layer size as the first number
            |and the output layer size as the last number.""".stripMargin)

    val weights =
      SparkSummaryEntry(
        name = "weights",
        value = sparkModel.weights,
        description = "The vector of perceptron layers' weights.")

    super.report(extended)
      .withAdditionalTable(CommonTablesGenerators.modelSummary(
        List(
          numberOfFeatures,
          layers,
          weights)))
  }

  override def tryToLoadModel(path: String): Option[SparkMultilayerPerceptronClassifierModel] = {
    ML.ModelLoading.multilayerPerceptronClassification(path)
  }
}
