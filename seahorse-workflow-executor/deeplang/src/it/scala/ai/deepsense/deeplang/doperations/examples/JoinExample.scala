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

import ai.deepsense.deeplang.doperations.Join
import ai.deepsense.deeplang.doperations.Join.ColumnPair
import ai.deepsense.deeplang.params.selections.{NameSingleColumnSelection, NameColumnSelection}

class JoinExample extends AbstractOperationExample[Join] {
  override def dOperation: Join = {
    new Join()
      .setLeftPrefix("left_")
      .setRightPrefix("right_")
      .setJoinColumns(Seq(ColumnPair()
        .setLeftColumn(NameSingleColumnSelection("city"))
        .setRightColumn(NameSingleColumnSelection("city"))))
  }

  override def fileNames: Seq[String] = Seq("example_city_price", "example_city_beds")
}
