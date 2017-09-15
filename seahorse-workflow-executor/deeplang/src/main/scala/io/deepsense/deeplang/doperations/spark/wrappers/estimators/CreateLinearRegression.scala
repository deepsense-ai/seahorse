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
import io.deepsense.deeplang.doperables.spark.wrappers.estimators.LinearRegression
import io.deepsense.deeplang.doperations.EstimatorAsFactory

class CreateLinearRegression extends EstimatorAsFactory[LinearRegression]
    with SparkOperationDocumentation {

  override val id: Id = "461a7b68-5fc8-4cd7-a912-0e0cc70eb3aa"
  override val name: String = "Linear Regression"
  override val description: String = "Creates a linear regression model"

  override protected[this] val docsGuideLocation =
    Some("ml-classification-regression.html#linear-regression")
  override val since: Version = Version(1, 0, 0)
}
