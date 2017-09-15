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

import scala.reflect.runtime.universe.TypeTag

import io.deepsense.commons.utils.Version
import io.deepsense.deeplang.DOperation.Id
import io.deepsense.deeplang.doperables.GetFromVectorTransformer

class GetFromVector extends TransformerAsOperation[GetFromVectorTransformer] {

  override val id: Id = "241a23d1-97a0-41d0-bcf7-5c2ccb24e3d5"
  override val name: String = "Get From Vector"
  override val description: String =
    "Extracts single number from vector column"

  override lazy val tTagTO_1: TypeTag[GetFromVectorTransformer] = typeTag

  override val since: Version = Version(1, 2, 0)
}
