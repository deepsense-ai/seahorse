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

package ai.deepsense.deeplang.doperations

import ai.deepsense.commons.utils.Version
import ai.deepsense.deeplang.DOperation.Id
import ai.deepsense.deeplang.doperables.dataframe.DataFrame
import ai.deepsense.deeplang.ExecutionContext

case class RNotebook()
  extends Notebook {

  override val id: Id = "89198bfd-6c86-40de-8238-68f7e0a0b50e"
  override val name: String = "R Notebook"
  override val description: String = "Creates an R notebook with access to the DataFrame"

  override val since: Version = Version(1, 3, 0)

  override val notebookType: String = "r"

  override protected def execute(dataFrame: DataFrame)(context: ExecutionContext): Unit = {
    context.dataFrameStorage.setInputDataFrame(0, dataFrame.sparkDataFrame)
    headlessExecution(context)
  }
}
