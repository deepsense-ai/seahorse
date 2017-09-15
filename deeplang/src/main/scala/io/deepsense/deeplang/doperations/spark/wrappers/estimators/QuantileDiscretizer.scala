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

package io.deepsense.deeplang.doperations.spark.wrappers.estimators

import io.deepsense.commons.utils.Version
import io.deepsense.deeplang.DOperation._
import io.deepsense.deeplang.documentation.SparkOperationDocumentation
import io.deepsense.deeplang.doperables.spark.wrappers.estimators.QuantileDiscretizerEstimator
import io.deepsense.deeplang.doperables.spark.wrappers.models.QuantileDiscretizerModel
import io.deepsense.deeplang.doperations.EstimatorAsOperation

class QuantileDiscretizer
  extends EstimatorAsOperation[QuantileDiscretizerEstimator, QuantileDiscretizerModel]
  with SparkOperationDocumentation {

  override val id: Id = "986e0b10-09de-44e9-a5b1-1dcc5fb53bd1"
  override val name: String = "Quantile Discretizer"
  override val description: String =
    "Takes a column with continuous features and outputs a column with binned categorical features."

  override protected[this] val docsGuideLocation =
    Some("ml-features.html#quantilediscretizer")
  override val since: Version = Version(1, 1, 0)
}
