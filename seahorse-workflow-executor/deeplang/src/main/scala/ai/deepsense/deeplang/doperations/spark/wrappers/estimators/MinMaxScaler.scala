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

package ai.deepsense.deeplang.doperations.spark.wrappers.estimators

import ai.deepsense.commons.utils.Version
import ai.deepsense.deeplang.DOperation.Id
import ai.deepsense.deeplang.documentation.SparkOperationDocumentation
import ai.deepsense.deeplang.doperables.spark.wrappers.estimators.MinMaxScalerEstimator
import ai.deepsense.deeplang.doperables.spark.wrappers.models.MinMaxScalerModel
import ai.deepsense.deeplang.doperations.EstimatorAsOperation

class MinMaxScaler extends EstimatorAsOperation[MinMaxScalerEstimator, MinMaxScalerModel]
    with SparkOperationDocumentation {

  override val id: Id = "a63b6de3-793b-4cbd-ae81-76de216d90d5"
  override val name: String = "Min-Max Scaler"
  override val description: String =
    """Linearly rescales each feature to a common range [min, max] using column summary statistics.
      |The operation is also known as Min-Max normalization or rescaling.""".stripMargin

  override protected[this] val docsGuideLocation =
    Some("ml-features.html#minmaxscaler")
  override val since: Version = Version(1, 0, 0)
}
