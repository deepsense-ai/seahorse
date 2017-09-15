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

import org.apache.spark.mllib.feature.{StandardScaler, StandardScalerModel}
import org.apache.spark.mllib.regression.{GeneralizedLinearAlgorithm, GeneralizedLinearModel, LabeledPoint}

import io.deepsense.commons.types.ColumnType
import io.deepsense.deeplang.doperables._

trait UntrainedLinearRegression[TrainedRegressionType <: Scorable, ModelType] {
  this: LinearModel =>

  protected def trainedRegressionCreator:
    (LinearRegressionParameters, ModelType, Seq[String], String, StandardScalerModel)
      => TrainedRegressionType

  protected def trainLinearRegression
  [T <: GeneralizedLinearModel](
    modelParameters: LinearRegressionParameters,
    trainParameters: Trainable.TrainingParameters,
    createModel: () => GeneralizedLinearAlgorithm[T]): Scorable = {

    trainParameters.labeledPoints.cache()

    val scaler: StandardScalerModel =
      new StandardScaler(withStd = true, withMean = true)
        .fit(trainParameters.labeledPoints.map(_.features))

    val scaledLabeledPoints =
      trainParameters.labeledPoints
        .map(lp => LabeledPoint(lp.label, scaler.transform(lp.features)))

    trainParameters.labeledPoints.unpersist()

    scaledLabeledPoints.cache()

    val result = trainedRegressionCreator(
      modelParameters,
      createModel().run(scaledLabeledPoints).asInstanceOf[ModelType],
      trainParameters.features,
      trainParameters.target,
      scaler)

    scaledLabeledPoints.unpersist()

    result
  }

  protected def generateUntrainedRegressionReport(
      modelParameters: LinearRegressionParameters): Report = {

    DOperableReporter("Report for " + getClass.getSimpleName)
      .withParameters(modelParameters)
      .report
  }
}
