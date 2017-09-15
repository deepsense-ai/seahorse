/**
 * Copyright 2015 deepsense.ai (CodiLime, Inc)
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

package ai.deepsense.deeplang.doperables.spark.wrappers.estimators

import scala.language.reflectiveCalls

import org.apache.spark.ml.feature.{StringIndexer => SparkStringIndexer, StringIndexerModel => SparkStringIndexerModel}

import ai.deepsense.deeplang.doperables.multicolumn.MultiColumnParams.SingleOrMultiColumnChoices.SingleColumnChoice
import ai.deepsense.deeplang.doperables.spark.wrappers.models.{MultiColumnStringIndexerModel, SingleColumnStringIndexerModel, StringIndexerModel}
import ai.deepsense.deeplang.doperables.{SparkMultiColumnEstimatorWrapper, SparkSingleColumnEstimatorWrapper}
import ai.deepsense.deeplang.params.Param

class StringIndexerEstimator
  extends SparkMultiColumnEstimatorWrapper[
    SparkStringIndexerModel,
    SparkStringIndexer,
    StringIndexerModel,
    SingleColumnStringIndexerModel,
    SingleStringIndexer,
    MultiColumnStringIndexerModel] {

  setDefault(singleOrMultiChoiceParam, SingleColumnChoice())

  override def getSpecificParams: Array[Param[_]] = Array()
}

class SingleStringIndexer
  extends SparkSingleColumnEstimatorWrapper[
    SparkStringIndexerModel,
    SparkStringIndexer,
    SingleColumnStringIndexerModel] {

  override def getSpecificParams: Array[Param[_]] = Array()
}

