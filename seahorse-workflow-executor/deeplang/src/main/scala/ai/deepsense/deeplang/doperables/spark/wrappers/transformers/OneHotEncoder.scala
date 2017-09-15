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

package ai.deepsense.deeplang.doperables.spark.wrappers.transformers

import org.apache.spark.ml.feature.{OneHotEncoder => SparkOneHotEncoder}

import ai.deepsense.deeplang.doperables.SparkTransformerAsMultiColumnTransformer
import ai.deepsense.deeplang.params.Param
import ai.deepsense.deeplang.params.wrappers.spark.BooleanParamWrapper

class OneHotEncoder extends SparkTransformerAsMultiColumnTransformer[SparkOneHotEncoder] {

  val dropLast = new BooleanParamWrapper[SparkOneHotEncoder](
    name = "drop last",
    description = Some("Whether to drop the last category in the encoded vector."),
    sparkParamGetter = _.dropLast)
  setDefault(dropLast, true)

  override protected def getSpecificParams: Array[Param[_]] = Array(dropLast)
}
