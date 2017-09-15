/**
 * Copyright 2016, deepsense.ai
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
import io.deepsense.deeplang.doperables.spark.wrappers.estimators.AFTSurvivalRegression
import io.deepsense.deeplang.doperations.EstimatorAsFactory

class CreateAFTSurvivalRegression extends EstimatorAsFactory[AFTSurvivalRegression]
    with SparkOperationDocumentation {

  override val id: Id = "e315aa7f-16f2-4fa5-8376-69a96171a57a"
  override val name: String = "AFT Survival Regression"
  override val description: String = "Creates an AFT survival regression model"

  override protected[this] val docsGuideLocation =
    Some("ml-classification-regression.html#survival-regression")
  override val since: Version = Version(1, 1, 0)
}
