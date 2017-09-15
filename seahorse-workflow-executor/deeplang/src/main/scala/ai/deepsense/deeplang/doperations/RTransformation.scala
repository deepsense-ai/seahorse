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

package ai.deepsense.deeplang.doperations

import ai.deepsense.commons.utils.Version
import ai.deepsense.deeplang.DOperation._
import ai.deepsense.deeplang.doperables.RTransformer
import scala.reflect.runtime.universe.TypeTag

import ai.deepsense.deeplang.documentation.OperationDocumentation

class RTransformation extends TransformerAsOperation[RTransformer] with OperationDocumentation {
  override val id: Id = "b578ad31-3a5b-4b94-a8d1-4c319fac6add"
  override val name: String = "R Transformation"
  override val description: String = "Creates a custom R transformation"

  override lazy val tTagTO_1: TypeTag[RTransformer] = typeTag

  override val since: Version = Version(1, 3, 0)
}

object RTransformation {
  val InputPortNumber: Int = 0
  val OutputPortNumber: Int = 0
}
