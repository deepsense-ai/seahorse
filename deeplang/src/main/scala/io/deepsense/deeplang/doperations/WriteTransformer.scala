/**
 * Copyright 2016, deepsense.io
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

import java.io.{File, IOException}

import scala.reflect.runtime.{universe => ru}

import io.deepsense.commons.utils.Version
import io.deepsense.commons.utils.FileOperations.deleteRecursivelyIfExists
import io.deepsense.deeplang.DOperation.Id
import io.deepsense.deeplang.documentation.OperationDocumentation
import io.deepsense.deeplang.doperables.Transformer
import io.deepsense.deeplang.doperations.exceptions.DeepSenseIOException
import io.deepsense.deeplang.params.{Params, StringParam}
import io.deepsense.deeplang.{DOperation1To0, ExecutionContext}

case class WriteTransformer()
  extends DOperation1To0[Transformer]
  with Params
  with OperationDocumentation {

  override val id: Id = "58368deb-68d0-4657-ae3f-145160cb1e2b"
  override val name: String = "Write Transformer"
  override val description: String = "Writes a Transformer to a directory"

  override val since: Version = Version(1, 1, 0)

  val outputPath = StringParam(
    name = "output path",
    description = "The output path for writing the Transformer.")

  def getOutputPath: String = $(outputPath)
  def setOutputPath(value: String): this.type = set(outputPath, value)

  val params = declareParams(outputPath)

  override protected def execute(transformer: Transformer)(context: ExecutionContext): Unit = {
    val outputDictPath = getOutputPath
    try {
      deleteRecursivelyIfExists(new File(outputDictPath))
      transformer.save(context, outputDictPath)
    } catch {
      case e: IOException =>
        logger.error(s"WriteTransformer error. Could not write transformer to the directory", e)
        throw DeepSenseIOException(e)
    }
  }
}

object WriteTransformer {
  def apply(outputPath: String): WriteTransformer = {
    new WriteTransformer().setOutputPath(outputPath)
  }
}
