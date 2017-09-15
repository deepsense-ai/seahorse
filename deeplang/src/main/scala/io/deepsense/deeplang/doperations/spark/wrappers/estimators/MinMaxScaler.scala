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
import io.deepsense.deeplang.doperables.spark.wrappers.estimators.MinMaxScalerEstimator
import io.deepsense.deeplang.doperations.EstimatorAsOperation

class MinMaxScaler extends EstimatorAsOperation[MinMaxScalerEstimator] {

  override val id: Id = "a63b6de3-793b-4cbd-ae81-76de216d90d5"
  override val name: String = "Min-Max Scaler"
  override val description: String = "Rescales each feature individually to a common " +
    "range [min, max] linearly using column summary statistics, " +
    "which is also known as min-max normalization or rescaling"
}
