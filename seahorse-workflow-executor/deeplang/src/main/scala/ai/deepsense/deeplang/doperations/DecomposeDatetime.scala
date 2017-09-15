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

package ai.deepsense.deeplang.doperations

import scala.reflect.runtime.universe.TypeTag

import ai.deepsense.commons.utils.Version
import ai.deepsense.deeplang.DOperation.Id
import ai.deepsense.deeplang.documentation.OperationDocumentation
import ai.deepsense.deeplang.doperables.DatetimeDecomposer

class DecomposeDatetime extends TransformerAsOperation[DatetimeDecomposer] with OperationDocumentation  {

  override val id: Id = "6c18b05e-7db7-4315-bce1-3291ed530675"
  override val name: String = "Decompose Datetime"
  override val description: String =
    "Extracts Numeric fields (year, month, etc.) from a Timestamp column"

  override lazy val tTagTO_1: TypeTag[DatetimeDecomposer] = typeTag

  override val since: Version = Version(0, 4, 0)
}
