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
import ai.deepsense.deeplang.DOperation.Id
import ai.deepsense.deeplang.documentation.SparkOperationDocumentation
import ai.deepsense.deeplang.doperables.spark.wrappers.estimators.LDA
import ai.deepsense.deeplang.doperations.EstimatorAsFactory

class CreateLDA extends EstimatorAsFactory[LDA]
    with SparkOperationDocumentation {

  override val id: Id = "a385f8fe-c64e-4d71-870a-9d5048747a3c"
  override val name: String = "LDA"
  override val description: String =
    """Latent Dirichlet Allocation (LDA), a topic model designed for text documents. LDA is given a
      |collection of documents as input data, via the `features column` parameter. Each document is
      |specified as a vector of length equal to the vocabulary size, where each entry is the count
      |for the corresponding term (word) in the document. Feature transformers such as Tokenize and
      |Count Vectorizer can be useful for converting text to word count vectors.""".stripMargin

  override protected[this] val docsGuideLocation =
    Some("ml-clustering.html#latent-dirichlet-allocation-lda")
  override val since: Version = Version(1, 1, 0)
}
