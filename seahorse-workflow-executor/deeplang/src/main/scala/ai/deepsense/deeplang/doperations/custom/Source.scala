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

package ai.deepsense.deeplang.doperations.custom

import scala.reflect.runtime.{universe => ru}

import ai.deepsense.commons.utils.Version
import ai.deepsense.deeplang.DOperation.Id
import ai.deepsense.deeplang.documentation.OperationDocumentation
import ai.deepsense.deeplang.doperables.dataframe.DataFrame
import ai.deepsense.deeplang.params.Param
import ai.deepsense.deeplang.{DOperation0To1, ExecutionContext}

case class Source() extends DOperation0To1[DataFrame] with OperationDocumentation {

  override val id: Id = Source.id
  override val name: String = "Source"
  override val description: String = "Custom transformer source"

  override val since: Version = Version(1, 0, 0)

  override val specificParams: Array[Param[_]] = Array()

  @transient
  override lazy val tTagTO_0: ru.TypeTag[DataFrame] = ru.typeTag[DataFrame]

  override protected def execute()(context: ExecutionContext): DataFrame =
    throw new IllegalStateException("should not be executed")
}

object Source {
  val id: Id = "f94b04d7-ec34-42f7-8100-93fe235c89f8"
}
