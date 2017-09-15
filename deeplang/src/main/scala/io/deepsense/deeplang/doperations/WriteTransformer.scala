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

import java.io.IOException

import scala.reflect.runtime.{universe => ru}

import io.deepsense.commons.utils.Version
import io.deepsense.deeplang.DOperation.Id
import io.deepsense.deeplang.doperables.Transformer
import io.deepsense.deeplang.doperations.exceptions.DeepSenseIOException
import io.deepsense.deeplang.params.{Params, StringParam}
import io.deepsense.deeplang.{DOperation1To0, ExecutionContext}

case class WriteTransformer()
  extends DOperation1To0[Transformer]
  with Params {

  override val id: Id = "58368deb-68d0-4657-ae3f-145160cb1e2b"
  override val name: String = "Write Transformer"
  override val description: String = "Writes a Transformer to a file"

  override val since: Version = Version(1, 1, 0)

  val outputPath = StringParam(
    name = "outputPath",
    description = "The output path.")

  def getOutputPath: String = $(outputPath)
  def setOutputFile(value: String): this.type = set(outputPath, value)

  val params = declareParams(outputPath)
  setDefault(outputPath, "file:///tmp/seahorse_transformer")

  override protected def _execute(context: ExecutionContext)(transformer: Transformer): Unit = {
    val outputDictPath = getOutputPath
    try {
      transformer.save(context, outputDictPath)
    } catch {
      case e: IOException =>
        logger.error(s"WriteTransformer error. Could not write transformer to file", e)
        throw DeepSenseIOException(e)
    }
  }

  @transient
  override lazy val tTagTI_0: ru.TypeTag[Transformer] = ru.typeTag[Transformer]
}

object WriteTransformer {
  def apply(outputFile: String): WriteTransformer = {
    new WriteTransformer().setOutputFile(outputFile)
  }
}
