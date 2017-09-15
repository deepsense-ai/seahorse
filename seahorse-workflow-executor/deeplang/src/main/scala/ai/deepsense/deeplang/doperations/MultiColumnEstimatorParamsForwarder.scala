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

package ai.deepsense.deeplang.doperations

import ai.deepsense.deeplang.doperables.MultiColumnEstimator

trait MultiColumnEstimatorParamsForwarder[E <: MultiColumnEstimator[_, _, _]] {
  self: EstimatorAsOperation[E, _] =>

  def setSingleColumn(inputColumnName: String, outputColumnName: String): this.type = {
    estimator.setSingleColumn(inputColumnName, outputColumnName)
    set(estimator.extractParamMap())
    this
  }

  def setMultipleColumn(inputColumnNames: Set[String], outputColumnPrefix: String): this.type = {
    estimator.setMultipleColumn(inputColumnNames, outputColumnPrefix)
    set(estimator.extractParamMap())
    this
  }
}
