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
import ai.deepsense.deeplang.DOperation.Id
import ai.deepsense.deeplang.doperables.SortTransformer
import scala.reflect.runtime.universe.{typeTag, TypeTag}

import ai.deepsense.deeplang.documentation.OperationDocumentation

class SortTransformation extends TransformerAsOperation[SortTransformer] with OperationDocumentation {
  override val id: Id = "1fa337cc-26f5-4cff-bd91-517777924d66"
  override val name: String = "Sort"
  override val description: String = "Sorts DataFrame by selected columns"

  override def since: Version = Version(1, 4, 0)

  @transient
  override lazy val tTagTO_1: TypeTag[SortTransformer] = typeTag[SortTransformer]
}
