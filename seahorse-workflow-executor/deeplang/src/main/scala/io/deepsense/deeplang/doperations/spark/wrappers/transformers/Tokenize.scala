/**
 * Copyright 2015, deepsense.ai
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

package io.deepsense.deeplang.doperations.spark.wrappers.transformers

import scala.reflect.runtime.universe.TypeTag

import io.deepsense.commons.utils.Version
import io.deepsense.deeplang.DOperation.Id
import io.deepsense.deeplang.documentation.SparkOperationDocumentation
import io.deepsense.deeplang.doperables.spark.wrappers.transformers.StringTokenizer
import io.deepsense.deeplang.doperations.TransformerAsOperation

class Tokenize extends TransformerAsOperation[StringTokenizer]
    with SparkOperationDocumentation {

  override val id: Id = "38751243-5e0e-435a-b366-8d225c9fd5ca"
  override val name: String = "Tokenize"
  override val description: String = "Converts text to lowercase and splits it by spaces"

  override lazy val tTagTO_1: TypeTag[StringTokenizer] = typeTag

  override protected[this] val docsGuideLocation =
    Some("ml-features.html#tokenizer")
  override val since: Version = Version(1, 0, 0)
}
