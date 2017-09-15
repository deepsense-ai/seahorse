/**
 * Copyright 2015, CodiLime Inc.
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

package io.deepsense.deeplang.doperables.file

import org.apache.spark.rdd.RDD

import io.deepsense.deeplang.doperables.Report
import io.deepsense.deeplang.{DOperable, ExecutionContext}
import io.deepsense.reportlib.model.{ReportContent, Table}

/**
 * Class representing lines of a file
 * @param rdd rdd representing lines of file
 * @param reportParams params that will be passed to the report
 *                     example Map("Size" -> "3GB", "Creation date" -> "2009/01/01 12:12")
 *                     TODO: DS-530 clarify reportParameters and build case class with validation
 */
case class File(
    rdd: Option[RDD[String]],
    reportParams: Option[Map[String, String]])
  extends DOperable {

  def this() = this(None, None)

  override def toInferrable: DOperable = new File()

  override def report(executionContext: ExecutionContext): Report = {
    val table = File.prepareReportTable(reportParams.get)
    Report(ReportContent("File details", List(table)))
  }

  override def save(executionContext: ExecutionContext)(path: String): Unit = ???
}

object File {
  def prepareReportTable(reportParams: Map[String, String]): Table = {
    // reportParams is a map so order of the parameters is not guaranteed in the final report
    val (keys, values) = reportParams.unzip
    Table("File details", "", None, Some(keys.toList), values.map(s => List(Option(s))).toList)
  }
}
