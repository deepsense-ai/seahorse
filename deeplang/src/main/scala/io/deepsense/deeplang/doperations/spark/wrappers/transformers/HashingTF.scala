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
import io.deepsense.deeplang.doperables.spark.wrappers.transformers.HashingTFTransformer
import io.deepsense.deeplang.doperations.TransformerAsOperation

class HashingTF extends TransformerAsOperation[HashingTFTransformer] {

  override val id: Id = "4266c9c0-6863-44ca-967b-62927ca34434"
  override val name: String = "HashingTF"
  override val description: String =
    "Maps a sequence of terms to term frequencies using the hashing trick"

  override lazy val tTagTO_1: TypeTag[HashingTFTransformer] = typeTag
}
