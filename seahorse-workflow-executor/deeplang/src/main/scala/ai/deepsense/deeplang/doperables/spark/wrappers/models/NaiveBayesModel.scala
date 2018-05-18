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

import org.apache.spark.ml.classification.{NaiveBayes => SparkNaiveBayes, NaiveBayesModel => SparkNaiveBayesModel}

import ai.deepsense.deeplang.ExecutionContext
import ai.deepsense.deeplang.doperables.SparkModelWrapper
import ai.deepsense.deeplang.doperables.report.CommonTablesGenerators.SparkSummaryEntry
import ai.deepsense.deeplang.doperables.report.{CommonTablesGenerators, Report}
import ai.deepsense.deeplang.doperables.serialization.SerializableSparkModel
import ai.deepsense.deeplang.doperables.spark.wrappers.params.common.ProbabilisticClassifierParams
import ai.deepsense.deeplang.params.Param

class NaiveBayesModel
  extends SparkModelWrapper[
    SparkNaiveBayesModel,
    SparkNaiveBayes]
  with ProbabilisticClassifierParams {

  override val params: Array[Param[_]] = Array(
    featuresColumn,
    probabilityColumn,
    rawPredictionColumn,
    predictionColumn)

  override def report(extended: Boolean = true): Report = {
    val pi = SparkSummaryEntry(
      name = "pi",
      value = sparkModel.pi,
      description = "Log of class priors, whose dimension is C (number of classes)")

    val theta = SparkSummaryEntry(
      name = "theta",
      value = sparkModel.theta,
      description = "Log of class conditional probabilities, " +
        "whose dimension is C (number of classes) by D (number of features)")

    super.report(extended)
      .withAdditionalTable(CommonTablesGenerators.modelSummary(List(pi) ++ List(theta)))
  }

  override protected def loadModel(
      ctx: ExecutionContext,
      path: String): SerializableSparkModel[SparkNaiveBayesModel] = {
    new SerializableSparkModel(SparkNaiveBayesModel.load(path))
  }
}
