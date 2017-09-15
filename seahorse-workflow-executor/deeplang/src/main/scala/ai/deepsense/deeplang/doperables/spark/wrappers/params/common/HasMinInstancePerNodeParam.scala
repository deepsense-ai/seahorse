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

package ai.deepsense.deeplang.doperables.spark.wrappers.params.common

import scala.language.reflectiveCalls

import org.apache.spark.ml
import org.apache.spark.ml.regression.RandomForestRegressor

import ai.deepsense.deeplang.params.Params
import ai.deepsense.deeplang.params.validators.RangeValidator
import ai.deepsense.deeplang.params.wrappers.spark.{IntParamWrapper, DoubleParamWrapper}

trait HasMinInstancePerNodeParam extends Params {

  val minInstancesPerNode =
    new IntParamWrapper[ml.param.Params { val minInstancesPerNode: ml.param.IntParam }](
      name = "min instances per node",
      description = Some("The minimum number of instances each child must have after split. " +
        "If a split causes the left or right child to have fewer instances than the parameter's " +
        "value, the split will be discarded as invalid."),
      sparkParamGetter = _.minInstancesPerNode,
      RangeValidator(1.0, Int.MaxValue, step = Some(1.0)))
  setDefault(minInstancesPerNode, 1.0)

}
