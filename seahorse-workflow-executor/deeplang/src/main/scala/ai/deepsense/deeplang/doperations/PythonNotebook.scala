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

package ai.deepsense.deeplang.doperations

import java.io.ByteArrayInputStream

import ai.deepsense.commons.utils.Version
import ai.deepsense.deeplang.DOperation.Id
import ai.deepsense.deeplang.ExecutionContext
import ai.deepsense.deeplang.doperables.dataframe.DataFrame
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.reflect.runtime.{universe => ru}
import scala.util.Failure

import ai.deepsense.commons.rest.client.NotebookRestClient

case class PythonNotebook()
  extends Notebook {

  override val id: Id = "e76ca616-0322-47a5-b390-70c9668265dd"
  override val name: String = "Python Notebook"
  override val description: String = "Creates a Python notebook with access to the DataFrame"

  override val since: Version = Version(1, 0, 0)
  override val notebookType: String = "python"

  override protected def execute(dataFrame: DataFrame)(context: ExecutionContext): Unit = {
    context.dataFrameStorage.setInputDataFrame(0, dataFrame.sparkDataFrame)
    headlessExecution(context)
  }

}

