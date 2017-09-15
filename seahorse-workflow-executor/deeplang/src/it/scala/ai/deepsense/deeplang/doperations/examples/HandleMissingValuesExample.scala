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

package ai.deepsense.deeplang.doperations.examples

import ai.deepsense.deeplang.doperables.MissingValuesHandler.MissingValueIndicatorChoice.No
import ai.deepsense.deeplang.doperables.MissingValuesHandler.Strategy.RemoveRow
import ai.deepsense.deeplang.doperations.HandleMissingValues
import ai.deepsense.deeplang.params.selections.{NameColumnSelection, MultipleColumnSelection}

class HandleMissingValuesExample extends AbstractOperationExample[HandleMissingValues]{
  override def dOperation: HandleMissingValues = {
    val op = new HandleMissingValues()
    op.transformer
        .setUserDefinedMissingValues(Seq("-1.0"))
      .setSelectedColumns(
        MultipleColumnSelection(Vector(NameColumnSelection(Set("baths", "price")))))
      .setStrategy(RemoveRow())
      .setMissingValueIndicator(No())
    op.set(op.transformer.extractParamMap())
  }

  override def fileNames: Seq[String] = Seq("example_missing_values")
}
