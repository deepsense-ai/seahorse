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

import java.nio.file.{Files, Path}

import org.scalatest.BeforeAndAfter
import ai.deepsense.deeplang.doperables.{PythonTransformer, TargetTypeChoices, TypeConverter}
import ai.deepsense.deeplang.doperations.exceptions.DeepSenseIOException

class WriteReadTransformerIntegSpec
  extends WriteReadTransformerIntegTest
  with BeforeAndAfter {

  val tempDir: Path = Files.createTempDirectory("writeReadTransformer")

  "ReadTransformer" should {
    "read previously written Transformer" in {
      val transformer =
        new TypeConverter().setTargetType(TargetTypeChoices.BooleanTargetTypeChoice())
      val outputPath: Path = tempDir.resolve("TypeConverter")

      writeReadTransformer(transformer, outputPath.toString)
    }
  }
  "WriteTransformer" should {
    "overwrite the previously written Transformer if the overwrite parameter is set to true" in {
      val transformer1 = new PythonTransformer()
      val transformer2 =
        new TypeConverter().setTargetType(TargetTypeChoices.BooleanTargetTypeChoice())
      val outputPath: Path = tempDir.resolve("TypeConverter")
      writeTransformer(transformer1, outputPath.toString, overwrite = true)
      writeReadTransformer(transformer2, outputPath.toString)
    }

    "throw an exception if a Transformer with the given name exists and the overwrite parameter is set to false" in {
      val transformer =
        new TypeConverter().setTargetType(TargetTypeChoices.BooleanTargetTypeChoice())
      val outputPath: Path = tempDir.resolve("TypeConverter")
      writeTransformer(transformer, outputPath.toString, overwrite = true)
      a [DeepSenseIOException] shouldBe thrownBy {
        writeTransformer(transformer, outputPath.toString, overwrite = false)
      }
    }
  }

  after {
    tempDir.toFile.delete()
  }
}
