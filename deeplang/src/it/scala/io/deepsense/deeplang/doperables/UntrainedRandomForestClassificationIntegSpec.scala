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

import io.deepsense.deeplang.PrebuiltTypedColumns.ExtendedColumnType
import io.deepsense.deeplang.PrebuiltTypedColumns.ExtendedColumnType.ExtendedColumnType
import io.deepsense.deeplang.doperables.machinelearning.randomforest.RandomForestParameters
import io.deepsense.deeplang.doperables.machinelearning.randomforest.classification.{TrainedRandomForestClassification, UntrainedRandomForestClassification}

class UntrainedRandomForestClassificationIntegSpec
  extends TrainableBaseIntegSpec("UntrainedRandomForestClassification") {

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
    ExtendedColumnType.boolean,
    ExtendedColumnType.categorical1,
    ExtendedColumnType.categorical2)

  override def unacceptableTargetTypes: Seq[ExtendedColumnType] = Seq(
    // this is omitted because it's a runtime problem, not schema problem
    //ExtendedColumnType.nonBinaryValuedNumeric
    ExtendedColumnType.string,
    ExtendedColumnType.timestamp,
    ExtendedColumnType.categoricalMany)

  override def createTrainableInstance: Trainable =
    UntrainedRandomForestClassification(RandomForestParameters(1, "auto", "entropy", 4, 100))
}
