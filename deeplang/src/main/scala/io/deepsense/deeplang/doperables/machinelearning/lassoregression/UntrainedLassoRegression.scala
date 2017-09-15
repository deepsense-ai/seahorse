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

package io.deepsense.deeplang.doperables.machinelearning.lassoregression

import org.apache.spark.mllib.feature.StandardScalerModel
import org.apache.spark.mllib.regression.{LassoModel, LassoWithSGD}

import io.deepsense.deeplang.doperables.ColumnTypesPredicates.Predicate
import io.deepsense.deeplang.doperables.Trainable.TrainingParameters
import io.deepsense.deeplang.doperables._
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.doperables.machinelearning.{LinearRegressionParameters, UntrainedLinearRegression}
import io.deepsense.deeplang.inference.{InferContext, InferenceWarnings}
import io.deepsense.deeplang.{DKnowledge, DOperable, ExecutionContext}

case class UntrainedLassoRegression(
    createModel: () => LassoWithSGD, modelParameters: LinearRegressionParameters)
  extends LassoRegression
  with UntrainedLinearRegression[TrainedLassoRegression, LassoModel]
  with Trainable {

  def this() = this(() => null, null)

  override def toInferrable: DOperable = new UntrainedLassoRegression()

  override protected def runTraining: RunTraining = runTrainingWithUncachedLabeledPoints

  override protected def actualTraining: TrainScorable = (trainParameters: TrainingParameters) => {
    trainLinearRegression(modelParameters, trainParameters, createModel)
  }

  override protected def actualInference(
      context: InferContext)(
      parameters: TrainableParameters)(
      dataFrame: DKnowledge[DataFrame]): (DKnowledge[Scorable], InferenceWarnings) =
    (DKnowledge(new TrainedLassoRegression), InferenceWarnings.empty)

  override def report(executionContext: ExecutionContext): Report =
    generateUntrainedRegressionReport(modelParameters)

  override def save(context: ExecutionContext)(path: String): Unit =
    throw new UnsupportedOperationException

  override protected def featurePredicate: Predicate = ColumnTypesPredicates.isNumeric
  override protected def labelPredicate: Predicate = ColumnTypesPredicates.isNumeric

  override protected def trainedRegressionCreator:
    (LinearRegressionParameters, LassoModel, Seq[String], String, StandardScalerModel)
      => TrainedLassoRegression = TrainedLassoRegression.apply
}
