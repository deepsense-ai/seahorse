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
import io.deepsense.deeplang.doperables.CustomPythonColumnOperationTransformer

class CustomPythonColumnOperation
  extends TransformerAsOperation[CustomPythonColumnOperationTransformer] {

  override val id: Id = "9951d301-7eb7-473b-81ad-0f8659619784"
  override val name: String = "Custom Python Column Operation"
  override val description: String = "Creates a custom Python column operation"

  override lazy val tTagTO_1: TypeTag[CustomPythonColumnOperationTransformer] = typeTag
}
