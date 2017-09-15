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
import ai.deepsense.deeplang.doperables.spark.wrappers.estimators.VectorIndexerEstimator
import ai.deepsense.deeplang.doperables.spark.wrappers.models.VectorIndexerModel
import ai.deepsense.deeplang.doperations.EstimatorAsOperation

class VectorIndexer extends EstimatorAsOperation[VectorIndexerEstimator, VectorIndexerModel]
    with SparkOperationDocumentation {

  override val id: Id = "d62abcbf-1540-4d58-8396-a92b017f2ef0"
  override val name: String = "Vector Indexer"
  override val description: String =
    """Vector Indexer indexes categorical features inside of a Vector. It decides which features
      |are categorical and converts them to category indices. The decision is based on the number of
      |distinct values of a feature.""".stripMargin

  override protected[this] val docsGuideLocation =
    Some("ml-features.html#vectorindexer")
  override val since: Version = Version(1, 0, 0)
}
