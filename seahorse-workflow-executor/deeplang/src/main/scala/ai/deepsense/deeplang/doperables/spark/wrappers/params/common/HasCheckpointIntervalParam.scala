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

package ai.deepsense.deeplang.doperables.spark.wrappers.params.common

import scala.language.reflectiveCalls

import org.apache.spark.ml

import ai.deepsense.deeplang.params.Params
import ai.deepsense.deeplang.params.validators.RangeValidator
import ai.deepsense.deeplang.params.wrappers.spark.IntParamWrapper

trait HasCheckpointIntervalParam extends Params {

  val checkpointInterval = new IntParamWrapper[
      ml.param.Params { val checkpointInterval: ml.param.IntParam }](
    name = "checkpoint interval",
    description = Some("""The checkpoint interval. E.g. 10 means that the cache will get checkpointed
        |every 10 iterations.""".stripMargin),
    sparkParamGetter = _.checkpointInterval,
    validator = RangeValidator(begin = 1.0, end = Int.MaxValue, step = Some(1.0)))
  setDefault(checkpointInterval, 10.0)
}
