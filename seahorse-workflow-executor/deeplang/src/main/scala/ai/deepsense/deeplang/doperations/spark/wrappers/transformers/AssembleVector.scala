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

package ai.deepsense.deeplang.doperations.spark.wrappers.transformers

import scala.reflect.runtime.universe.TypeTag

import ai.deepsense.commons.utils.Version
import ai.deepsense.deeplang.DOperation.Id
import ai.deepsense.deeplang.documentation.SparkOperationDocumentation
import ai.deepsense.deeplang.doperables.spark.wrappers.transformers.VectorAssembler
import ai.deepsense.deeplang.doperations.TransformerAsOperation

class AssembleVector extends TransformerAsOperation[VectorAssembler]
    with SparkOperationDocumentation {

  override val id: Id = "c57a5b99-9184-4095-9037-9359f905628d"
  override val name: String = "Assemble Vector"
  override val description: String = "Merges multiple columns into a single vector column"

  override lazy val tTagTO_1: TypeTag[VectorAssembler] = typeTag

  override protected[this] val docsGuideLocation =
    Some("ml-features.html#vectorassembler")
  override val since: Version = Version(1, 0, 0)
}
