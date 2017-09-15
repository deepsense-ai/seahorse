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
import io.deepsense.deeplang.doperables.spark.wrappers.estimators.VanillaDecisionTreeClassifier
import io.deepsense.deeplang.doperations.EstimatorAsFactory

class CreateDecisionTreeClassifier
  extends EstimatorAsFactory[VanillaDecisionTreeClassifier]
  with SparkOperationDocumentation {

  override val id: Id = "81039036-bb26-445b-81b5-63fbc9295c00"
  override val name: String = "Decision Tree Classifier"
  override val description: String =
    """Creates a decision tree classifier.
      |It supports both binary and multiclass labels,
      |as well as both continuous and categorical features.""".stripMargin

  override protected[this] val docsGuideLocation =
    Some("ml-classification-regression.html#decision-trees")
  override val since: Version = Version(1, 1, 0)
}
