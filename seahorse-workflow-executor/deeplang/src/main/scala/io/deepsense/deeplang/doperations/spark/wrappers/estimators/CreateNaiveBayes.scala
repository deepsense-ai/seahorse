/**
 * Copyright 2016, deepsense.io
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
import io.deepsense.deeplang.doperables.spark.wrappers.estimators.NaiveBayes
import io.deepsense.deeplang.doperations.EstimatorAsFactory

class CreateNaiveBayes extends EstimatorAsFactory[NaiveBayes] with SparkOperationDocumentation {

  override val id: Id = "63de675b-b4ec-41a4-985f-2e0bafafe3c4"
  override val name: String = "Naive Bayes"
  override val description: String =
    """Creates a naive Bayes model.
      |It supports Multinomial NB which can handle finitely supported discrete data.
      |For example, by converting documents into TF-IDF vectors,
      |it can be used for document classification.
      |By making every vector a binary (0/1) data, it can also be used as Bernoulli NB.
      |The input feature values must be nonnegative.""".stripMargin


  override protected[this] val docsGuideLocation = Some("mllib-naive-bayes.html")
  override val since: Version = Version(1, 1, 0)
}
