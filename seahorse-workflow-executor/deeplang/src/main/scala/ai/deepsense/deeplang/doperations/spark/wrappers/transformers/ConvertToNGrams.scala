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
import ai.deepsense.deeplang.doperables.spark.wrappers.transformers.NGramTransformer
import ai.deepsense.deeplang.doperations.TransformerAsOperation

class ConvertToNGrams extends TransformerAsOperation[NGramTransformer]
    with SparkOperationDocumentation {

  override val id: Id = "06a73bfe-4e1a-4cde-ae6c-ad5a31f72496"
  override val name: String = "Convert To n-grams"
  override val description: String = "Converts arrays of strings to arrays of n-grams. Null " +
    "values in the input arrays are ignored. Each n-gram is represented by a space-separated " +
    "string of words. When the input is empty, an empty array is returned. When the input array " +
    "is shorter than n (number of elements per n-gram), no n-grams are returned."

  override lazy val tTagTO_1: TypeTag[NGramTransformer] = typeTag

  override protected[this] val docsGuideLocation =
    Some("ml-features.html#n-gram")
  override val since: Version = Version(1, 0, 0)
}
