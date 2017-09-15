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

package ai.deepsense.deeplang.doperables.spark.wrappers.transformers

import java.nio.file.{Files, Path}

import org.apache.commons.io.FileUtils
import org.scalatest.{BeforeAndAfter, Suite}

import ai.deepsense.deeplang.doperables.Transformer
import ai.deepsense.deeplang.doperables.dataframe.DataFrame
import ai.deepsense.deeplang.{DeeplangIntegTestSupport, ExecutionContext}

trait TransformerSerialization extends Suite with BeforeAndAfter {

  var tempDir: Path = _

  before {
    tempDir = Files.createTempDirectory("writeReadTransformer")
  }

  after {
    FileUtils.deleteDirectory(tempDir.toFile)
  }
}

object TransformerSerialization {

  implicit class TransformerSerializationOps(private val transformer: Transformer) {

    def applyTransformationAndSerialization(
        path: Path,
        df: DataFrame)(implicit executionContext: ExecutionContext): DataFrame = {
      val result = transformer._transform(executionContext, df)
      val deserialized = loadSerializedTransformer(path)
      val resultFromSerializedTransformer = deserialized._transform(executionContext, df)
      DeeplangIntegTestSupport.assertDataFramesEqual(result, resultFromSerializedTransformer)
      result
    }

    def loadSerializedTransformer(
        path: Path)(
        implicit executionContext: ExecutionContext): Transformer = {
      val outputPath: Path = path.resolve(this.getClass.getName)
      transformer.save(executionContext, outputPath.toString)
      Transformer.load(executionContext, outputPath.toString)
    }
  }
}
