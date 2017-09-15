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

import org.apache.spark.ml
import org.apache.spark.ml.feature.{Bucketizer => SparkQuantileDiscretizerModel, QuantileDiscretizer => SparkQuantileDiscretizer}

import ai.deepsense.deeplang.doperables.SparkSingleColumnEstimatorWrapper
import ai.deepsense.deeplang.doperables.spark.wrappers.models.QuantileDiscretizerModel
import ai.deepsense.deeplang.params.Param
import ai.deepsense.deeplang.params.validators.RangeValidator
import ai.deepsense.deeplang.params.wrappers.spark.IntParamWrapper

class QuantileDiscretizerEstimator
  extends SparkSingleColumnEstimatorWrapper[
    SparkQuantileDiscretizerModel,
    SparkQuantileDiscretizer,
    QuantileDiscretizerModel] {

  val numBuckets = new IntParamWrapper[ml.param.Params { val numBuckets: ml.param.IntParam }](
    name = "num buckets",
    description = Some("Maximum number of buckets (quantiles or categories) " +
      "into which the data points are grouped. Must be >= 2."),
    sparkParamGetter = _.numBuckets,
    RangeValidator(2.0, Int.MaxValue, step = Some(1.0)))
  setDefault(numBuckets, 2.0)

  override protected def getSpecificParams: Array[Param[_]] = Array(numBuckets)

  def setNumBuckets(value: Int): this.type = set(numBuckets -> value)
}
