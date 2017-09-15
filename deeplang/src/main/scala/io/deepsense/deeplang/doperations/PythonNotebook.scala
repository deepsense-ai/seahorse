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

import scala.reflect.runtime.{universe => ru}

import io.deepsense.commons.utils.Version
import io.deepsense.deeplang.DOperation.Id
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.{DOperation1To0, ExecutionContext}

case class PythonNotebook()
  extends DOperation1To0[DataFrame] {

  override val id: Id = "e76ca616-0322-47a5-b390-70c9668265dd"
  override val name: String = "Python Notebook"
  override val description: String = "Creates a Python notebook with access to the DataFrame"

  override val since: Version = Version(1, 0, 0)

  override val params = declareParams()

  override protected def _execute(context: ExecutionContext)(dataFrame: DataFrame): Unit = ()

  @transient
  override lazy val tTagTI_0: ru.TypeTag[DataFrame] = ru.typeTag[DataFrame]
}
