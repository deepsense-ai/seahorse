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
import io.deepsense.deeplang.doperables.spark.wrappers.transformers.NormalizerTransformer
import io.deepsense.deeplang.doperations.TransformerAsOperation

class Normalize extends TransformerAsOperation[NormalizerTransformer] {

  override val id: Id = "20f3d9ef-9b04-49c6-8acd-7ddafdedcb39"
  override val name: String = "Normalizer"
  override val description: String = "Normalize vector columns using given p-norm"

  override lazy val tTagTO_1: TypeTag[NormalizerTransformer] = typeTag
}
