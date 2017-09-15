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

package io.deepsense.deeplang.doperables.machinelearning.ridgeregression

import org.apache.spark.mllib.feature.{StandardScaler, StandardScalerModel}
import org.apache.spark.mllib.regression.{LabeledPoint, RidgeRegressionWithSGD}

import io.deepsense.deeplang._
import io.deepsense.deeplang.doperables.ColumnTypesPredicates.Predicate
import io.deepsense.deeplang.doperables.Trainable.Parameters
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.doperables.{ColumnTypesPredicates, Report, Scorable, Trainable}
import io.deepsense.deeplang.inference.{InferContext, InferenceWarnings}
import io.deepsense.reportlib.model.ReportContent

case class UntrainedRidgeRegression(
    createModel: () => RidgeRegressionWithSGD)
  extends RidgeRegression with Trainable {

  def this() = this(() => null)

  override def toInferrable: DOperable = new UntrainedRidgeRegression()

  override protected def runTraining: RunTraining = runTrainingWithUncachedLabeledPoints

  override protected def actualTraining: TrainScorable = (trainParameters) => {
    trainParameters.labeledPoints.cache()

    val scaler: StandardScalerModel =
      new StandardScaler(withStd = true, withMean = true)
        .fit(trainParameters.labeledPoints.map(_.features))

    val scaledLabeledPoints =
      trainParameters.labeledPoints
        .map(lp => LabeledPoint(lp.label, scaler.transform(lp.features)))

    trainParameters.labeledPoints.unpersist()

    scaledLabeledPoints.cache()

    val result = TrainedRidgeRegression(
      createModel().run(scaledLabeledPoints),
      trainParameters.features,
      trainParameters.target,
      scaler)

    scaledLabeledPoints.unpersist()

    result
  }

  override protected def actualInference(
      context: InferContext)(
      parameters: Parameters)(
      dataFrame: DKnowledge[DataFrame]): (DKnowledge[Scorable], InferenceWarnings) =
    (DKnowledge(new TrainedRidgeRegression), InferenceWarnings.empty)

  override def report(executionContext: ExecutionContext): Report =
    Report(ReportContent("Report for UntrainedRidgeRegression"))

  override def save(context: ExecutionContext)(path: String): Unit =
    throw new UnsupportedOperationException

  override protected def featurePredicate: Predicate = ColumnTypesPredicates.isNumeric
  override protected def labelPredicate: Predicate = ColumnTypesPredicates.isNumeric
}
