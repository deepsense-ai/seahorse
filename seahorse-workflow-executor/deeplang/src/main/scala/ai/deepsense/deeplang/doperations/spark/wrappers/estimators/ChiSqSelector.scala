/**
 * Copyright 2016 deepsense.ai (CodiLime, Inc)
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

package ai.deepsense.deeplang.doperations.spark.wrappers.estimators

import ai.deepsense.commons.utils.Version
import ai.deepsense.deeplang.DOperation._
import ai.deepsense.deeplang.documentation.SparkOperationDocumentation
import ai.deepsense.deeplang.doperables.spark.wrappers.estimators.ChiSqSelectorEstimator
import ai.deepsense.deeplang.doperables.spark.wrappers.models.ChiSqSelectorModel
import ai.deepsense.deeplang.doperations.EstimatorAsOperation

class ChiSqSelector
  extends EstimatorAsOperation[ChiSqSelectorEstimator, ChiSqSelectorModel]
  with SparkOperationDocumentation {

  override val id: Id = "7355518a-4581-4048-b8b2-880cdb212205"
  override val name: String = "Chi-Squared Selector"
  override val description: String =
    "Selects categorical features to use for predicting a categorical label."

  override protected[this] val docsGuideLocation =
    Some("ml-features.html#chisqselector")
  override val since: Version = Version(1, 1, 0)
}
