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

package io.deepsense.deeplang.doperables.machinelearning.logisticregression

import io.deepsense.commons.types.ColumnType
import io.deepsense.commons.types.ColumnType.ColumnType
import io.deepsense.commons.utils.DoubleUtils
import io.deepsense.deeplang.doperables.machinelearning.ModelParameters

case class LogisticRegressionParameters(
    regularization: Double, iterationsNumber: Double, tolerance: Double)
  extends ModelParameters {

    override def reportTableRows: Seq[(String, ColumnType, String)] =
      Seq(
        ("Regularization", ColumnType.numeric, DoubleUtils.double2String(regularization)),
        ("Iterations number", ColumnType.numeric, DoubleUtils.double2String(iterationsNumber)),
        ("Tolerance", ColumnType.numeric, DoubleUtils.double2String(tolerance)))
}
