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

import scala.reflect.runtime.universe.TypeTag

import ai.deepsense.commons.utils.Version
import ai.deepsense.deeplang.DOperation.Id
import ai.deepsense.deeplang.documentation.OperationDocumentation
import ai.deepsense.deeplang.doperables.RowsFilterer

class FilterRows extends TransformerAsOperation[RowsFilterer] with OperationDocumentation {

  override val id: Id = "7d7eddfa-c9be-48c3-bb8c-5f7cc59b403a"
  override val name: String = "Filter Rows"
  override val description: String =
    "Creates a DataFrame containing only rows satisfying given condition"

  override lazy val tTagTO_1: TypeTag[RowsFilterer] = typeTag

  override val since: Version = Version(1, 0, 0)
}
