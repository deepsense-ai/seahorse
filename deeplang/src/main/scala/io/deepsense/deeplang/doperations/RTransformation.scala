/**
 * Copyright 2016, deepsense.io
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

package io.deepsense.deeplang.doperations

import io.deepsense.commons.utils.Version
import io.deepsense.deeplang.DOperation._
import io.deepsense.deeplang.doperables.RTransformer

import scala.reflect.runtime.universe.TypeTag

class RTransformation extends TransformerAsOperation[RTransformer] {
  override val id: Id = "b578ad31-3a5b-4b94-a8d1-4c319fac6add"
  override val name: String = "R Transformation"
  override val description: String = "Creates a custom R transformation"

  override val since: Version = Version(1, 3, 0)
}

object RTransformation {
  val InputPortNumber: Int = 0
  val OutputPortNumber: Int = 0
}
