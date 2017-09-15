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

import io.deepsense.commons.utils.Version
import io.deepsense.deeplang.DOperation._
import io.deepsense.deeplang.documentation.SparkOperationDocumentation
import io.deepsense.deeplang.doperables.spark.wrappers.transformers.PolynomialExpander
import io.deepsense.deeplang.doperations.TransformerAsOperation

class PolynomialExpand extends TransformerAsOperation[PolynomialExpander]
    with SparkOperationDocumentation {

  override val id: Id = "4a741088-3180-4373-940d-741b2f1620de"
  override val name: String = "Polynomial Expansion"
  override val description: String = "Applies polynomial expansion to vector columns"

  override protected[this] val docsGuideLocation =
    Some("ml-features.html#polynomialexpansion")
  override val since: Version = Version(1, 0, 0)
}
