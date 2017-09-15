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
import ai.deepsense.deeplang.doperables.spark.wrappers.estimators.CountVectorizerEstimator
import ai.deepsense.deeplang.doperables.spark.wrappers.models.CountVectorizerModel
import ai.deepsense.deeplang.doperations.EstimatorAsOperation

class CountVectorizer extends EstimatorAsOperation[CountVectorizerEstimator, CountVectorizerModel]
    with SparkOperationDocumentation {

  override val id: Id = "e640d7df-d464-4ac0-99c4-235c29a0aa31"
  override val name: String = "Count Vectorizer"
  override val description: String =
    """Extracts the vocabulary from a given collection of documents and generates a vector
      |of token counts for each document.""".stripMargin

  override protected[this] val docsGuideLocation =
    Some("ml-features.html#countvectorizer")
  override val since: Version = Version(1, 0, 0)
}
