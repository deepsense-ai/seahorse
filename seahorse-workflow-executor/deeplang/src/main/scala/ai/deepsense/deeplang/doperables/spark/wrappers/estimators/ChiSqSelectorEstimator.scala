/**
 * Copyright 2016 deepsense.ai (CodiLime, Inc)
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

import org.apache.spark.ml
import org.apache.spark.ml.feature.{ChiSqSelector => SparkChiSqSelector, ChiSqSelectorModel => SparkChiSqSelectorModel}

import ai.deepsense.deeplang.doperables.SparkEstimatorWrapper
import ai.deepsense.deeplang.doperables.spark.wrappers.models.ChiSqSelectorModel
import ai.deepsense.deeplang.doperables.spark.wrappers.params.common.{HasFeaturesColumnParam, HasLabelColumnParam, HasOutputColumn}
import ai.deepsense.deeplang.params.Param
import ai.deepsense.deeplang.params.validators.RangeValidator
import ai.deepsense.deeplang.params.wrappers.spark.IntParamWrapper

class ChiSqSelectorEstimator
  extends SparkEstimatorWrapper[
    SparkChiSqSelectorModel,
    SparkChiSqSelector,
    ChiSqSelectorModel]
  with HasFeaturesColumnParam
  with HasOutputColumn
  with HasLabelColumnParam{

  val numTopFeatures = new IntParamWrapper[
    ml.param.Params { val numTopFeatures: ml.param.IntParam }](
    name = "num top features",
    description = Some("Number of features that selector will select, ordered by statistics value " +
      "descending. If the real number of features is lower, then this will select all " +
      "features."),
    sparkParamGetter = _.numTopFeatures,
    validator = RangeValidator(begin = 1.0, end = Int.MaxValue, step = Some(1.0)))
  setDefault(numTopFeatures -> 50)

  override val params: Array[Param[_]] = Array(
    numTopFeatures,
    featuresColumn,
    outputColumn,
    labelColumn)

  def setNumTopFeatures(value: Int): this.type = set(numTopFeatures -> value)
}
