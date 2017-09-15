/**
 * Copyright 2015, deepsense.io
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

package io.deepsense.deeplang.doperations.examples

import java.io.{FileOutputStream, PrintWriter, File}
import java.nio.channels.FileChannel

import scala.collection.mutable

import org.apache.spark.sql.Row
import spray.json._

import io.deepsense.commons.utils.Logging
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.doperations.ReadDataFrame
import io.deepsense.deeplang.doperations.inout.CsvParameters.ColumnSeparatorChoice.Comma
import io.deepsense.deeplang.params.{Param, Params}
import io.deepsense.deeplang.{DOperable, DOperation, DeeplangIntegTestSupport}

abstract class AbstractOperationExample[T <: DOperation]
    extends DeeplangIntegTestSupport
    with Logging {

  def dOperation: T

  final def className: String = dOperation.getClass.getSimpleName

  def fileNames: Seq[String] = Seq.empty

  def loadCsv(fileName: String): DataFrame = {
    ReadDataFrame(
      this.getClass.getResource(s"/csv/$fileName.csv").getPath,
      Comma(),
      csvNamesIncluded = true,
      csvConvertToBoolean = false
    ).execute(executionContext)(Vector.empty[DOperable])
      .head
      .asInstanceOf[DataFrame]
  }

  def inputDataFrames: Seq[DataFrame] = fileNames.map(loadCsv)

  className should {
    "successfully run execute() and generate example" in {
      val op = dOperation
      val outputDfs = op
        .execute(executionContext)(inputDataFrames.toVector)
        .collect { case df: DataFrame => df }
      val html =
        ExampleHtmlFormatter.exampleHtml(op, inputDataFrames, outputDfs)

      val examplePageFile = new File(
        "../docs/operations/examples/" + className + ".md")

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
