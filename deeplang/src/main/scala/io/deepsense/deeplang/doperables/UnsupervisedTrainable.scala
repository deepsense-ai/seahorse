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

package io.deepsense.deeplang.doperables

import io.deepsense.deeplang.doperables.UnsupervisedTrainable.TrainingParameters
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.inference.{InferContext, InferenceWarnings}
import io.deepsense.deeplang.{DKnowledge, DMethod1To1, DOperable, ExecutionContext}

trait UnsupervisedTrainable extends DOperable {
  val train: DMethod1To1[UnsupervisedTrainableParameters, DataFrame, Scorable] =
    new DMethod1To1[UnsupervisedTrainableParameters, DataFrame, Scorable] {

      override def apply(context: ExecutionContext)
                        (parameters: UnsupervisedTrainableParameters)
                        (dataFrame: DataFrame): Scorable = {

        runTraining(context, parameters, dataFrame)(actualTraining)
      }

      override def infer(context: InferContext)
                        (parameters: UnsupervisedTrainableParameters)
                        (dataFrame: DKnowledge[DataFrame])
                        : (DKnowledge[Scorable], InferenceWarnings) = {

        actualInference(context)(parameters)(dataFrame)
      }
    }

  protected type TrainScorable = TrainingParameters => Scorable

  protected type RunTraining =
    (ExecutionContext, UnsupervisedTrainableParameters, DataFrame) =>
      (TrainScorable => Scorable)

  /**
   * This is the main method of train()
   * It should be, if possible, overridden with one of the runTraining* methods
   * defined in Trainable trait. They provide a frame for execution of actual training.
   */
  protected def runTraining: RunTraining

  /**
   * This method should be overridden with the actual execution of training.
   * It accepts [[TrainingParameters]] and returns a [[Scorable]] instance.
   */
  protected def actualTraining: TrainScorable

  protected def actualInference(context: InferContext)
                               (parameters: UnsupervisedTrainableParameters)
                               (dataFrame: DKnowledge[DataFrame])
                               : (DKnowledge[Scorable], InferenceWarnings)

  /**
   * The predicate that the prediction column has to meet.
   */
  protected def predictionPredicate: ColumnTypesPredicates.Predicate

  /**
   * The predicate all feature columns have to meet.
   */
  protected def featurePredicate: ColumnTypesPredicates.Predicate
}

object UnsupervisedTrainable {
  case class TrainingParameters(
    dataFrame: DataFrame,
    features: Seq[String],
    prediction: String)
}
