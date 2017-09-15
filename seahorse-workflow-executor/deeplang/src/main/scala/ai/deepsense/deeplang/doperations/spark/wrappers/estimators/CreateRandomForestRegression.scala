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
import ai.deepsense.deeplang.doperables.spark.wrappers.estimators.RandomForestRegression
import ai.deepsense.deeplang.doperations.EstimatorAsFactory

class CreateRandomForestRegression extends EstimatorAsFactory[RandomForestRegression]
    with SparkOperationDocumentation {

  override val id: Id = "2ec65504-bbe2-4ba2-a9b4-192e2f45ff16"
  override val name: String = "Random Forest Regression"
  override val description: String = "Random forest regression (RFR) is a learning " +
    "algorithm for regression. It supports both continuous and categorical features."

  override protected[this] val docsGuideLocation =
    Some("ml-classification-regression.html#random-forest-regression")
  override val since: Version = Version(1, 0, 0)
}
