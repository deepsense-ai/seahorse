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
import ai.deepsense.deeplang.doperables.spark.wrappers.estimators.GBTClassifier
import ai.deepsense.deeplang.doperables.spark.wrappers.models.GBTClassificationModel
import ai.deepsense.deeplang.doperations.EstimatorAsFactory

class CreateGBTClassifier extends EstimatorAsFactory[GBTClassifier]
    with SparkOperationDocumentation {

  override val id: Id = "98275271-9817-4add-85d7-e6eade3e5b81"
  override val name: String = "GBT Classifier"
  override val description: String =
    "Gradient-Boosted Trees (GBTs) is a learning algorithm for classification." +
      " It supports binary labels, as well as both continuous and categorical features." +
      " Note: Multiclass labels are not currently supported."

  override protected[this] val docsGuideLocation =
    Some("ml-classification-regression.html#gradient-boosted-tree-classifier")
  override val since: Version = Version(1, 0, 0)
}
