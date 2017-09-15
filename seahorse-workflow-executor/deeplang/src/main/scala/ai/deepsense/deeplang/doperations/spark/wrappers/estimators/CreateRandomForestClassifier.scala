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
import ai.deepsense.deeplang.doperables.spark.wrappers.estimators.{RandomForestClassifier, LogisticRegression}
import ai.deepsense.deeplang.doperations.EstimatorAsFactory

class CreateRandomForestClassifier extends EstimatorAsFactory[RandomForestClassifier]
    with SparkOperationDocumentation {

  override val id: Id = "7cd334e2-bd40-42db-bea1-7592f12302f2"
  override val name: String = "Random Forest Classifier"
  override val description: String = "Creates a random forest classification model"

  override protected[this] val docsGuideLocation =
    Some("ml-classification-regression.html#random-forest-classifier")
  override val since: Version = Version(1, 1, 0)
}
