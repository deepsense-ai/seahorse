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

import org.apache.spark.mllib.regression.{GeneralizedLinearAlgorithm, RidgeRegressionModel, RidgeRegressionWithSGD}
import org.scalactic.EqualityPolicy.Spread

import io.deepsense.deeplang.PrebuiltTypedColumns.ExtendedColumnType
import io.deepsense.deeplang.PrebuiltTypedColumns.ExtendedColumnType.ExtendedColumnType
import io.deepsense.deeplang.doperables.machinelearning.ridgeregression.{TrainedRidgeRegression, UntrainedRidgeRegression}
import io.deepsense.deeplang.doperables.machinelearning.LinearRegressionParameters
import io.deepsense.deeplang.doperables.machinelearning.ridgeregression.{TrainedRidgeRegression, UntrainedRidgeRegression}


class UntrainedRidgeRegressionIntegSpec
  extends TrainableBaseIntegSpec("UntrainedRidgeRegression")
  with GeneralizedLinearModelTrainableBaseIntegSpec[RidgeRegressionModel] {

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
    ExtendedColumnType.nonBinaryValuedNumeric)

  override def unacceptableTargetTypes: Seq[ExtendedColumnType] = Seq(
    ExtendedColumnType.categorical1,
    ExtendedColumnType.categorical2,
    ExtendedColumnType.categoricalMany,
    ExtendedColumnType.boolean,
    ExtendedColumnType.string,
    ExtendedColumnType.timestamp)

  override def createTrainableInstanceWithModel(
    untrainedModel: GeneralizedLinearAlgorithm[RidgeRegressionModel]): Trainable =
    UntrainedRidgeRegression(
      () => untrainedModel.asInstanceOf[RidgeRegressionWithSGD],
      mock[LinearRegressionParameters])

  override def mockUntrainedModel(): GeneralizedLinearAlgorithm[RidgeRegressionModel] =
    mock[RidgeRegressionWithSGD]

  // This needs overriding, because in RR features are scaled before
  // being passed to Spark model for training.
  override val expectedFeaturesInvocation: Seq[Spread[Double]] =
    Seq(-1.16 +- 0.01, -0.38 +- 0.01, 0.38 +- 0.01, 1.16 +- 0.01)

  override def createTrainableInstance: Trainable = {
    val model = new RidgeRegressionWithSGD()
    model.optimizer.setNumIterations(1)
    UntrainedRidgeRegression(() => model, LinearRegressionParameters(1, 1, 1))
  }
}
