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

package ai.deepsense.deeplang.doperables.spark.wrappers.params

import scala.language.reflectiveCalls

import org.apache.spark.ml

import ai.deepsense.deeplang.doperables.spark.wrappers.params.common._
import ai.deepsense.deeplang.params.Params
import ai.deepsense.deeplang.params.validators.RangeValidator
import ai.deepsense.deeplang.params.wrappers.spark.IntParamWrapper

trait Word2VecParams extends Params
  with HasMaxIterationsParam
  with HasStepSizeParam
  with HasSeedParam {

  val vectorSize = new IntParamWrapper[ml.param.Params { val vectorSize: ml.param.IntParam }](
    name = "vector size",
    description = Some("The dimension of codes after transforming from words."),
    sparkParamGetter = _.vectorSize,
    validator = RangeValidator.positiveIntegers)
  setDefault(vectorSize -> 100)

  val numPartitions = new IntParamWrapper[ml.param.Params { val numPartitions: ml.param.IntParam }](
    name = "num partitions",
    description = Some("The number of partitions for sentences of words."),
    sparkParamGetter = _.numPartitions,
    validator = RangeValidator.positiveIntegers)
  setDefault(numPartitions -> 1)

  val minCount = new IntParamWrapper[ml.param.Params { val minCount: ml.param.IntParam }](
    name = "min count",
    description = Some("The minimum number of occurences of a token to " +
      "be included in the model's vocabulary."),
    sparkParamGetter = _.minCount,
    validator = RangeValidator.positiveIntegers)
  setDefault(minCount -> 5)

  def setMinCount(value: Int): this.type = {
    set(minCount -> value)
  }

  def setVectorSize(value: Int): this.type = {
    set(vectorSize -> value)
  }
}
