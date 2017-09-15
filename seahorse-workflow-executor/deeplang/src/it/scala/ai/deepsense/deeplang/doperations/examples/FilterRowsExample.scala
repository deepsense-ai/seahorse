/**
 * Copyright 2016 deepsense.ai (CodiLime, Inc)
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

import ai.deepsense.deeplang.doperations.FilterRows

class FilterRowsExample extends AbstractOperationExample[FilterRows] {
  override def dOperation: FilterRows = {
    val op = new FilterRows()
    op.transformer
      .setCondition("0.4 < temp AND windspeed < 0.3")
    op.set(op.transformer.extractParamMap())
  }

  override def fileNames: Seq[String] = Seq("example_datetime_windspeed_hum_temp")
}
