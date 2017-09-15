/**
 * Copyright 2015, deepsense.io
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

import io.deepsense.deeplang.DOperation.Id
import io.deepsense.deeplang.doperables.spark.wrappers.transformers.NGramTransformer
import io.deepsense.deeplang.doperations.TransformerAsOperation

class ConvertToNGrams extends TransformerAsOperation[NGramTransformer] {

  override val id: Id = "06a73bfe-4e1a-4cde-ae6c-ad5a31f72496"
  override val name: String = "Convert to n-grams"
  override val description: String = "Converts arrays of strings to arrays of n-grams. " +
    "Null values in the input arrays are ignored. " +
    "Each n-gram is represented by a space-separated string of words."

  override lazy val tTagTO_1: TypeTag[NGramTransformer] = typeTag
}
