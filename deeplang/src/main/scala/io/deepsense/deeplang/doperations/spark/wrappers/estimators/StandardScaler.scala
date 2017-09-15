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

import io.deepsense.deeplang.DOperation.Id
import io.deepsense.deeplang.doperables.spark.wrappers.estimators.StandardScalerEstimator
import io.deepsense.deeplang.doperations.EstimatorAsOperation

class StandardScaler extends EstimatorAsOperation[StandardScalerEstimator] {

  override val id: Id = "85007b46-210c-4e88-b7dc-cf46d3803b06"
  override val name: String = "Standard Scaler"
  override val description: String = "Standardizes features by removing the mean and scaling " +
    "to unit variance using column summary statistics on the samples in the training set"
}
