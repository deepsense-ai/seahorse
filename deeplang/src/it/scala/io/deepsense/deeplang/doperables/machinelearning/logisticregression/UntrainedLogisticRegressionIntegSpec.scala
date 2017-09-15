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

package io.deepsense.deeplang.doperables.machinelearning.logisticregression

import org.apache.spark.mllib.classification.{LogisticRegressionModel, LogisticRegressionWithLBFGS}
import org.apache.spark.mllib.regression.GeneralizedLinearAlgorithm

import io.deepsense.deeplang.PrebuiltTypedColumns.ExtendedColumnType
import io.deepsense.deeplang.PrebuiltTypedColumns.ExtendedColumnType.ExtendedColumnType
import io.deepsense.deeplang.doperables.machinelearning.GeneralizedLinearModelTrainableBaseIntegSpec
import io.deepsense.deeplang.doperables.{Trainable, TrainableBaseIntegSpec}

class UntrainedLogisticRegressionIntegSpec
  extends TrainableBaseIntegSpec("UntrainedLogisticRegression")
  with GeneralizedLinearModelTrainableBaseIntegSpec[LogisticRegressionModel] {

  override def createTrainableInstanceWithModel(
      untrainedModelMock: GeneralizedLinearAlgorithm[LogisticRegressionModel]): Trainable =
    UntrainedLogisticRegression(
      mock[LogisticRegressionParameters],
      () => untrainedModelMock.asInstanceOf[LogisticRegressionWithLBFGS])

  override def mockUntrainedModel(): GeneralizedLinearAlgorithm[LogisticRegressionModel] =
    mock[LogisticRegressionWithLBFGS]

 override def acceptedFeatureTypes: Seq[ExtendedColumnType] = Seq(
    ExtendedColumnType.binaryValuedNumeric,
    ExtendedColumnType.nonBinaryValuedNumeric)

  override def unacceptableFeatureTypes: Seq[ExtendedColumnType] = Seq(
    ExtendedColumnType.categorical1,
    ExtendedColumnType.categorical2,
    ExtendedColumnType.categoricalMany,
    ExtendedColumnType.boolean,
    ExtendedColumnType.string,
    ExtendedColumnType.timestamp)

  override def acceptedTargetTypes: Seq[ExtendedColumnType] = Seq(
    ExtendedColumnType.binaryValuedNumeric,
    ExtendedColumnType.boolean,
    ExtendedColumnType.categorical1,
    ExtendedColumnType.categorical2)

  override def unacceptableTargetTypes: Seq[ExtendedColumnType] = Seq(
    // this is omitted because it's a runtime problem, not schema problem
    //ExtendedColumnType.nonBinaryValuedNumeric
    ExtendedColumnType.string,
    ExtendedColumnType.timestamp,
    ExtendedColumnType.categoricalMany)

  override def createTrainableInstance: Trainable = {
    val model = new LogisticRegressionWithLBFGS()
    model.optimizer.setNumIterations(1)
    UntrainedLogisticRegression(LogisticRegressionParameters(1, 1, 1), () => model)
  }
}
