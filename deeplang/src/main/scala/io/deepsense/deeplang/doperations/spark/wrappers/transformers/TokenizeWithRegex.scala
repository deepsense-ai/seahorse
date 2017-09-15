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

import io.deepsense.deeplang.DOperation._
import io.deepsense.deeplang.doperables.spark.wrappers.transformers.RegexTokenizer
import io.deepsense.deeplang.doperations.TransformerAsOperation

class TokenizeWithRegex extends TransformerAsOperation[RegexTokenizer] {

  override val id: Id = "3fb50e0a-d4fb-474f-b6f3-679788068b1b"
  override val name: String = "Tokenize with Regex"
  override val description: String = "Splits text using a regular expression"

  override lazy val tTagTO_1: TypeTag[RegexTokenizer] = typeTag
}
