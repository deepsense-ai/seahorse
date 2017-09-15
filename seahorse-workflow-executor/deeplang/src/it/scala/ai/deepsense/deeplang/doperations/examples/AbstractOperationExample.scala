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

package ai.deepsense.deeplang.doperations.examples

import java.io.{File, PrintWriter}

import ai.deepsense.commons.utils.Logging
import ai.deepsense.deeplang.doperables.dataframe.DataFrame
import ai.deepsense.deeplang.doperations.ReadDataFrame
import ai.deepsense.deeplang.doperations.inout.CsvParameters.ColumnSeparatorChoice.Comma
import ai.deepsense.deeplang.doperations.readwritedataframe.FileScheme
import ai.deepsense.deeplang.{DOperable, DOperation, DeeplangIntegTestSupport}

abstract class AbstractOperationExample[T <: DOperation]
    extends DeeplangIntegTestSupport
    with Logging {

  def dOperation: T

  final def className: String = dOperation.getClass.getSimpleName

  def fileNames: Seq[String] = Seq.empty

  def loadCsv(fileName: String): DataFrame = {
    ReadDataFrame(
      FileScheme.File.pathPrefix + this.getClass.getResource(s"/test_files/$fileName.csv").getPath,
      Comma(),
      csvNamesIncluded = true,
      csvConvertToBoolean = false
    ).executeUntyped(Vector.empty[DOperable])(executionContext)
      .head
      .asInstanceOf[DataFrame]
  }

  def inputDataFrames: Seq[DataFrame] = fileNames.map(loadCsv)

  className should {
    "successfully run execute() and generate example" in {
      val op = dOperation
      val outputDfs = op
        .executeUntyped(inputDataFrames.toVector)(executionContext)
        .collect { case df: DataFrame => df }
      val html =
        ExampleHtmlFormatter.exampleHtml(op, inputDataFrames, outputDfs)

      // TODO Make it not rely on relative path it's run from
      val examplePageFile = new File(
        "docs/operations/examples/" + className + ".md")

      examplePageFile.getParentFile.mkdirs()
      examplePageFile.createNewFile()

      val writer = new PrintWriter(examplePageFile)
      // scalastyle:off println
      writer.println(html)
      // scalastyle:on println
      writer.flush()
      writer.close()
      logger.info(
        "Created doc page for " + className)
    }
  }
}
