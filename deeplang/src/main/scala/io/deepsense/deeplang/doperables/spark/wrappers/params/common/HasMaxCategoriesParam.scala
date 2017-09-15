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

package io.deepsense.deeplang.doperables.spark.wrappers.params.common

import scala.language.reflectiveCalls

import org.apache.spark.ml

import io.deepsense.deeplang.params.Params
import io.deepsense.deeplang.params.validators.RangeValidator
import io.deepsense.deeplang.params.wrappers.spark.IntParamWrapper

trait HasMaxCategoriesParam extends Params {

  val maxCategories = new IntParamWrapper[ml.param.Params { val maxCategories: ml.param.IntParam }](
    name = "max categories",
    description = "Threshold for the number of values a categorical feature can take. " +
      "If a feature is found to have > maxCategories values, then it is declared continuous. " +
      "Must be >= 2",
    sparkParamGetter = _.maxCategories,
    validator = RangeValidator(begin = 2.0, end = Int.MaxValue, step = Some(1.0)))
  setDefault(maxCategories, 20.0)
}
