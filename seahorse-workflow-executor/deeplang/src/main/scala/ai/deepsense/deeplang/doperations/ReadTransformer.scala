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

import java.io._

import scala.reflect.runtime.{universe => ru}

import ai.deepsense.commons.utils.Version
import ai.deepsense.deeplang.DOperation.Id
import ai.deepsense.deeplang.documentation.OperationDocumentation
import ai.deepsense.deeplang.doperables.Transformer
import ai.deepsense.deeplang.doperations.ReadTransformer.ReadTransformerParameters
import ai.deepsense.deeplang.doperations.exceptions.DeepSenseIOException
import ai.deepsense.deeplang.params.{Params, StringParam}
import ai.deepsense.deeplang.{DOperation0To1, ExecutionContext}

case class ReadTransformer()
  extends DOperation0To1[Transformer]
  with Params
  with ReadTransformerParameters
  with OperationDocumentation {

  override val id: Id = "424dc996-a471-482d-b08c-bc12849f0b68"
  override val name: String = "Read Transformer"
  override val description: String = "Reads a Transformer from a directory"

  override val since: Version = Version(1, 1, 0)

  val specificParams: Array[ai.deepsense.deeplang.params.Param[_]] = Array(sourcePath)

  override protected def execute()(context: ExecutionContext): Transformer = {
    val path = getSourcePath
    try {
      Transformer.load(context, path)
    } catch {
      case e: IOException => throw DeepSenseIOException(e)
    }
  }

  @transient
  override lazy val tTagTO_0: ru.TypeTag[Transformer] = ru.typeTag[Transformer]
}

object ReadTransformer {
  trait ReadTransformerParameters {
    this: Params =>

    val sourcePath = StringParam(
      name = "source",
      description = Some("A path to the Transformer directory."))

    def getSourcePath: String = $(sourcePath)
    def setSourcePath(value: String): this.type = set(sourcePath, value)
  }

  def apply(sourcePath: String): ReadTransformer = {
    new ReadTransformer().setSourcePath(sourcePath)
  }
}
