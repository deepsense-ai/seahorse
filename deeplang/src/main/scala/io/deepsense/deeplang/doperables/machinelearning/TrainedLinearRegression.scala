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

package io.deepsense.deeplang.doperables.machinelearning

import org.apache.spark.mllib.regression.GeneralizedLinearModel

import io.deepsense.commons.types.ColumnType
import io.deepsense.deeplang.doperables.{DOperableReporter, LinearModel, Report, VectorScoring}

trait TrainedLinearRegression {
  this: LinearModel with VectorScoring =>

  def generateTrainedRegressionReport(
      modelParameters: LinearRegressionParameters,
      model: GeneralizedLinearModel,
      featureColumns: Seq[String],
      targetColumn: String): Report = {

    val reportName = "Report for " + getClass.getSimpleName

    DOperableReporter(reportName)
      .withParameters(
        description = "",
        ("Regularization parameter",
          ColumnType.numeric, modelParameters.regularizationParameter.toString),
        ("Iterations number", ColumnType.numeric, modelParameters.numberOfIterations.toString),
        ("Mini batch fraction", ColumnType.numeric, modelParameters.miniBatchFraction.toString)
      )
      .withWeights(featureColumns, model.weights.toArray)
      .withIntercept(model.intercept)
      .withVectorScoring(this)
      .report
  }
}
