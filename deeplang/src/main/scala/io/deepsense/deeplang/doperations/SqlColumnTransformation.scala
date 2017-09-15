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
import io.deepsense.deeplang.doperables.SqlColumnTransformer

case class SqlColumnTransformation()
  extends TransformerAsOperation[SqlColumnTransformer] {

  override val id: Id = "012876d9-7a72-47f9-98e4-8ed26db14d6d"
  override val name: String = "SQL Column Transformation"
  override val description: String =
    "Executes a SQL transformation on a column of a DataFrame"

  override val since: Version = Version(1, 1, 0)
}
