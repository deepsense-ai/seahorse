/**
 * Copyright (c) 2015, CodiLime, Inc.
 */

package io.deepsense.deeplang.doperables.file

import org.apache.spark.rdd.RDD

import io.deepsense.deeplang.{ExecutionContext, DOperable}
import io.deepsense.deeplang.doperables.Report
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

  override def report: Report = {
    val table = File.prepareReportTable(reportParams.get)
    Report(ReportContent("File details", Map("details" -> table)))
  }

  override def save(executionContext: ExecutionContext)(path: String): Unit = ???
}

object File {
  def prepareReportTable(reportParams: Map[String, String]): Table = {
    // reportParams is a map so order of the parameters is not guaranteed in the final report
    val (keys, values) = reportParams.unzip
    Table("File details", "", None, Some(keys.toList), values.map(List(_)).toList)
  }
}
