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
import ai.deepsense.deeplang.doperables.spark.wrappers.estimators.IsotonicRegression
import ai.deepsense.deeplang.doperations.EstimatorAsFactory

class CreateIsotonicRegression extends EstimatorAsFactory[IsotonicRegression]
    with SparkOperationDocumentation {

  override val id: Id = "0aebeb36-058c-49ef-a1be-7974ef56b564"
  override val name: String = "Isotonic Regression"
  override val description: String = "Creates an isotonic regression model"

  override protected[this] val docsGuideLocation =
    Some("mllib-isotonic-regression.html")
  override val since: Version = Version(1, 0, 0)
}
