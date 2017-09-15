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

package ai.deepsense.deeplang.doperations.exceptions

import ai.deepsense.commons.types.ColumnType
import ColumnType.ColumnType

case class WrongColumnTypeException(override val message: String)
  extends DOperationExecutionException(message, None)

object WrongColumnTypeException {
  def apply(
      columnName: String,
      actualType: ColumnType,
      expectedTypes: ColumnType*): WrongColumnTypeException =
    WrongColumnTypeException(
      s"Column '$columnName' has type '$actualType' instead of " +
        s"expected ${expectedTypes.map(t => s"'${t.toString}'").mkString(" or ")}.")
}
