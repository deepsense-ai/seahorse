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

package io.deepsense.deeplang.doperations

import scala.reflect.runtime.universe.TypeTag

import io.deepsense.deeplang.DOperation.Id
import io.deepsense.deeplang.doperables.ColumnsFilterer

class FilterColumns extends TransformerAsOperation[ColumnsFilterer] {

  override val id: Id = "6534f3f4-fa3a-49d9-b911-c213d3da8b5d"
  override val name: String = "Filter Columns"
  override val description: String =
    "Creates a DataFrame containing only selected columns"

  override lazy val tTagTO_1: TypeTag[ColumnsFilterer] = typeTag
}
