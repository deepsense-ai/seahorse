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

package ai.deepsense.deeplang.doperables.multicolumn

import ai.deepsense.deeplang.params.{Param, Params}

/**
 * Specific params are transformation params that do not describe
 * what data will be transformed but how the transformation will
 * behave. Specific params do NOT include params that define
 * input or output columns.
 */
trait HasSpecificParams {
  self: Params =>

  protected def getSpecificParams: Array[Param[_]]
}
