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
import io.deepsense.deeplang.DOperation.Id
import io.deepsense.deeplang.documentation.SparkOperationDocumentation
import io.deepsense.deeplang.doperables.spark.wrappers.estimators.Word2VecEstimator
import io.deepsense.deeplang.doperables.spark.wrappers.models.Word2VecModel
import io.deepsense.deeplang.doperations.EstimatorAsOperation

class Word2Vec extends EstimatorAsOperation[Word2VecEstimator, Word2VecModel]
    with SparkOperationDocumentation {

  override val id: Id = "131c6765-6b60-44c7-9a09-0f79fbb4ad2f"
  override val name: String = "Word2Vec"
  override val description: String =
    """Transforms vectors of words into vectors of numeric codes for the purpose of further
      |processing by NLP or machine learning algorithms.""".stripMargin

  override protected[this] val docsGuideLocation =
    Some("ml-features.html#word2vec")
  override val since: Version = Version(1, 0, 0)
}
