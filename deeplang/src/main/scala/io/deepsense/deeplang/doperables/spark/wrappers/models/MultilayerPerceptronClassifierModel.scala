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

import org.apache.spark.ml.classification.{MultilayerPerceptronClassificationModel => SparkMultilayerPerceptronClassifierModel, MultilayerPerceptronClassifier => SparkMultilayerPerceptronClassifier}

import io.deepsense.deeplang.doperables.SparkModelWrapper
import io.deepsense.deeplang.doperables.report.CommonTablesGenerators.SparkSummaryEntry
import io.deepsense.deeplang.doperables.report.{CommonTablesGenerators, Report}
import io.deepsense.deeplang.doperables.spark.wrappers.params.common.PredictorParams
import io.deepsense.deeplang.params.Param

class MultilayerPerceptronClassifierModel
  extends SparkModelWrapper[
    SparkMultilayerPerceptronClassifierModel,
    SparkMultilayerPerceptronClassifier]
  with PredictorParams {

  override val params: Array[Param[_]] = declareParams(
    featuresColumn,
    predictionColumn)

  override def report: Report = {
    val numberOfFeatures =
      SparkSummaryEntry(
        name = "number of features",
        value = model.numFeatures,
        description = "Number of features.")

    val layers =
      SparkSummaryEntry(
        name = "layers",
        value = model.layers,
        description =
          """The list of layer sizes that includes the input layer size as the first number
            |and the output layer size as the last number.""".stripMargin)

    val weights =
      SparkSummaryEntry(
        name = "weights",
        value = model.weights,
        description = "The vector of perceptron layers' weights.")

    super.report
      .withAdditionalTable(CommonTablesGenerators.modelSummary(
        List(
          numberOfFeatures,
          layers,
          weights)))
  }
}
