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

import ai.deepsense.deeplang.params.{BooleanParam, Params}

trait HasIsLargerBetterParam extends Params {

  val isLargerBetterParam = BooleanParam(
    name = "is larger better",
    description =
      Some("""Indicates whether the returned metric
        |is better to be maximized or minimized.""".stripMargin))
  setDefault(isLargerBetterParam -> false)
}
