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

import io.deepsense.commons.utils.Version
import io.deepsense.deeplang.DOperation.Id
import io.deepsense.deeplang.documentation.OperationDocumentation
import io.deepsense.deeplang.doperables.SqlTransformer

class SqlTransformation extends TransformerAsOperation[SqlTransformer] with OperationDocumentation {

  override val id: Id = "6cba4400-d966-4a2a-8356-b37f37b4c73f"
  override val name: String = "SQL Transformation"
  override val description: String =
    "Executes an SQL transformation on a DataFrame"

  override val since: Version = Version(0, 4, 0)
}
