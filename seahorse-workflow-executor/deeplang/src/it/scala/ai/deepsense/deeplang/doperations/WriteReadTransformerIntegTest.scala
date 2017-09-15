/**
 * Copyright 2017 deepsense.ai (CodiLime, Inc)
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

import ai.deepsense.deeplang.DeeplangIntegTestSupport
import ai.deepsense.deeplang.doperables.Transformer

abstract class WriteReadTransformerIntegTest extends DeeplangIntegTestSupport {

  def writeReadTransformer(transformer: Transformer, outputFile: String): Unit = {

    val writeTransformer: WriteTransformer = WriteTransformer(outputFile).setShouldOverwrite(true)
    val readTransformer: ReadTransformer = ReadTransformer(outputFile)

    writeTransformer.executeUntyped(Vector(transformer))(executionContext)
    val deserializedTransformer =
      readTransformer.executeUntyped(Vector())(executionContext).head.asInstanceOf[Transformer]

    deserializedTransformer shouldBe transformer
  }

  def writeTransformer(transformer: Transformer, outputFile: String, overwrite: Boolean): Unit = {
    val writeTransformer: WriteTransformer = WriteTransformer(outputFile).setShouldOverwrite(overwrite)
    writeTransformer.executeUntyped(Vector(transformer))(executionContext)
  }
}
