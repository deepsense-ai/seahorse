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

import org.apache.spark.mllib.regression.{RidgeRegressionModel, RidgeRegressionWithSGD}

import io.deepsense.deeplang._
import io.deepsense.deeplang.doperables.ColumnTypesPredicates.Predicate
import io.deepsense.deeplang.doperables._
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.doperables.machinelearning.{LinearRegressionParameters, UntrainedLinearRegression}
import io.deepsense.deeplang.doperables.{ColumnTypesPredicates, Report, Scorable}
import io.deepsense.deeplang.inference.{InferContext, InferenceWarnings}

case class UntrainedRidgeRegression(
    createModel: () => RidgeRegressionWithSGD, modelParameters: LinearRegressionParameters)
  extends RidgeRegression
  with Trainable
  with UntrainedLinearRegression[TrainedRidgeRegression, RidgeRegressionModel] {

  def this() = this(() => null, null)

  override def toInferrable: DOperable = new UntrainedRidgeRegression()

  override protected def runTraining: RunTraining = runTrainingWithUncachedLabeledPoints

  override protected def actualTraining: TrainScorable =
    (trainParameters: Trainable.TrainingParameters) =>
      trainLinearRegression(
        modelParameters,
        trainParameters,
        createModel)

  override protected def actualInference(
      context: InferContext)(
      parameters: TrainableParameters)(
      dataFrame: DKnowledge[DataFrame]): (DKnowledge[Scorable], InferenceWarnings) =
    (DKnowledge(new TrainedRidgeRegression), InferenceWarnings.empty)

  override def report(executionContext: ExecutionContext): Report =
    generateUntrainedRegressionReport(modelParameters)

  override def save(context: ExecutionContext)(path: String): Unit =
    throw new UnsupportedOperationException

  override protected def featurePredicate: Predicate = ColumnTypesPredicates.isNumeric
  override protected def labelPredicate: Predicate = ColumnTypesPredicates.isNumeric

  override protected def trainedRegressionCreator = TrainedRidgeRegression.apply
}
