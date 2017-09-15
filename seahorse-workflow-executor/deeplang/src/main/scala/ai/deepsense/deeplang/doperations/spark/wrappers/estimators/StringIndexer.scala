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
import ai.deepsense.deeplang.DOperation._
import ai.deepsense.deeplang.documentation.SparkOperationDocumentation
import ai.deepsense.deeplang.doperables.spark.wrappers.estimators.StringIndexerEstimator
import ai.deepsense.deeplang.doperables.spark.wrappers.models.StringIndexerModel
import ai.deepsense.deeplang.doperations.{EstimatorAsOperation, MultiColumnEstimatorParamsForwarder}

class StringIndexer
  extends EstimatorAsOperation[StringIndexerEstimator, StringIndexerModel]
    with MultiColumnEstimatorParamsForwarder[StringIndexerEstimator]
    with SparkOperationDocumentation {

  override val id: Id = "c9df7000-9ea0-41c0-b66c-3062fd57851b"
  override val name: String = "String Indexer"
  override val description: String =
    "Maps a string column of labels to an integer column of label indices"

  override protected[this] val docsGuideLocation =
    Some("ml-features.html#stringindexer")
  override val since: Version = Version(1, 0, 0)
}
