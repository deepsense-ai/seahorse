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

package ai.deepsense.deeplang.doperations.inout

import ai.deepsense.deeplang.params.{BooleanParam, Params}

trait HasShouldConvertToBooleanParam {
  this: Params =>

  val shouldConvertToBoolean = BooleanParam(
    name = "convert to boolean",
    description = Some("Should columns containing only 0 and 1 be converted to Boolean?"))
  setDefault(shouldConvertToBoolean, false)

  def getShouldConvertToBoolean: Boolean = $(shouldConvertToBoolean)
  def setShouldConvertToBoolean(value: Boolean): this.type =
    set(shouldConvertToBoolean, value)
}
