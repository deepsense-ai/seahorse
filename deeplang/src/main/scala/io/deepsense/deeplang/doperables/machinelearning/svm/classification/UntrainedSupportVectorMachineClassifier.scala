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

package io.deepsense.deeplang.doperables.machinelearning.svm.classification

import org.apache.spark.mllib.classification.SVMWithSGD
import org.apache.spark.mllib.optimization.{L1Updater, SimpleUpdater, SquaredL2Updater, Updater}

import io.deepsense.commons.types.ColumnType
import io.deepsense.commons.utils.DoubleUtils
import io.deepsense.deeplang.doperables.ColumnTypesPredicates.Predicate
import io.deepsense.deeplang.doperables._
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.doperables.machinelearning.svm.SupportVectorMachineParameters
import io.deepsense.deeplang.inference.{InferContext, InferenceWarnings}
import io.deepsense.deeplang.parameters.RegularizationType
import io.deepsense.deeplang.{DKnowledge, DOperable, ExecutionContext}

case class UntrainedSupportVectorMachineClassifier(
    svmParameters: SupportVectorMachineParameters)
  extends SupportVectorMachineClassifier with Trainable {

  def this() = this(null)

  override protected def runTraining: RunTraining = runTrainingWithLabeledPoints

  override protected def actualTraining: TrainScorable = (trainParameters) => {
    val updater: Updater = svmParameters.regularization match {
      case RegularizationType.NONE => new SimpleUpdater
      case RegularizationType.L1 => new L1Updater
      case RegularizationType.L2 => new SquaredL2Updater
    }

    val svmWithSGD = new SVMWithSGD()
    svmWithSGD.optimizer
      .setUpdater(updater)
      .setNumIterations(svmParameters.numIterations)
      .setRegParam(svmParameters.regParam)
      .setMiniBatchFraction(svmParameters.miniBatchFraction)

    TrainedSupportVectorMachineClassifier(
      svmParameters,
      svmWithSGD.run(trainParameters.labeledPoints),
      trainParameters.features,
      trainParameters.target)
  }

  override protected def actualInference(
      context: InferContext)(
      parameters: TrainableParameters)(
      dataFrame: DKnowledge[DataFrame]): (DKnowledge[Scorable], InferenceWarnings) =
    (DKnowledge(new TrainedSupportVectorMachineClassifier()), InferenceWarnings.empty)

  override def save(executionContext: ExecutionContext)(path: String): Unit = ???

  override def toInferrable: DOperable = new UntrainedSupportVectorMachineClassifier()

  override def report(executionContext: ExecutionContext): Report = {
    DOperableReporter("Report for Untrained SVM Classification")
      .withParameters(svmParameters)
      .report
  }

  override protected def labelPredicate: Predicate = ColumnTypesPredicates.isNumericOrBinaryValued
  override protected def featurePredicate: Predicate = ColumnTypesPredicates.isNumeric
}
