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
import io.deepsense.deeplang.doperables.spark.wrappers.estimators.KMeans
import io.deepsense.deeplang.doperations.EstimatorAsFactory

class CreateKMeans extends EstimatorAsFactory[KMeans]
    with SparkOperationDocumentation {

  override val id: Id = "2ecdd789-695d-4efa-98ad-63c80ae70f71"
  override val name: String = "K-Means"
  override val description: String =
    "Creates a k-means model. Note: Trained k-means model does not have any parameters."

  override protected[this] val docsGuideLocation =
    Some("ml-clustering.html#k-means")
  override val since: Version = Version(1, 0, 0)
}
