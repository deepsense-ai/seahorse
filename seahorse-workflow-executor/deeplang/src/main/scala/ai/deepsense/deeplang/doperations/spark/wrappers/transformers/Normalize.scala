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
import ai.deepsense.deeplang.doperables.spark.wrappers.transformers.Normalizer
import ai.deepsense.deeplang.doperations.TransformerAsOperation

class Normalize extends TransformerAsOperation[Normalizer]
    with SparkOperationDocumentation {

  override val id: Id = "20f3d9ef-9b04-49c6-8acd-7ddafdedcb39"
  override val name: String = "Normalize"
  override val description: String = "Normalizes vector columns using given p-norm"

  override lazy val tTagTO_1: TypeTag[Normalizer] = typeTag

  override protected[this] val docsGuideLocation =
    Some("ml-features.html#normalizer")
  override val since: Version = Version(1, 0, 0)
}
