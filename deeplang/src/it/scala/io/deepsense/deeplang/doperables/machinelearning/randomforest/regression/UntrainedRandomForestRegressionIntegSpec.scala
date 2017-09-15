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

package io.deepsense.deeplang.doperables.machinelearning.randomforest.regression

import io.deepsense.deeplang.PrebuiltTypedColumns.ExtendedColumnType
import io.deepsense.deeplang.PrebuiltTypedColumns.ExtendedColumnType.ExtendedColumnType
import io.deepsense.deeplang.doperables.machinelearning.randomforest.RandomForestParameters
import io.deepsense.deeplang.doperables.{Trainable, TrainableBaseIntegSpec}

class UntrainedRandomForestRegressionIntegSpec
  extends TrainableBaseIntegSpec("UntrainedRandomForestRegression") {

  override def acceptedFeatureTypes: Seq[ExtendedColumnType] = Seq(
    ExtendedColumnType.binaryValuedNumeric,
    ExtendedColumnType.nonBinaryValuedNumeric,
    ExtendedColumnType.categorical2,
    ExtendedColumnType.categoricalMany)

  override def unacceptableFeatureTypes: Seq[ExtendedColumnType] = Seq(
    ExtendedColumnType.categorical1,
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

  override def createTrainableInstance: Trainable =
    UntrainedRandomForestRegression(RandomForestParameters(1, "auto", "variance", 4, 100))
}
