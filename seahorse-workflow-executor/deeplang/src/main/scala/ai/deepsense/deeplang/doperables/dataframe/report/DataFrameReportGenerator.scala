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

package ai.deepsense.deeplang.doperables.dataframe.report

import org.apache.spark.mllib.stat.{MultivariateStatisticalSummary, Statistics}
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.{DataFrame, Row}

import ai.deepsense.commons.types.{ColumnType, SparkConversions}
import ai.deepsense.deeplang.doperables.dataframe.report.distribution.{DistributionCalculator, NoDistributionReasons}
import ai.deepsense.deeplang.doperables.report.{Report, ReportUtils}
import ai.deepsense.deeplang.utils.SparkTypeConverter
import ai.deepsense.reportlib.model._

object DataFrameReportGenerator {

  val ReportContentName: String = "DataFrame Report"

  val DataSampleTableName = "Data Sample"
  val DataSchemaTableName = "Column Names and Types"
  val DataFrameSizeTableName = "DataFrame Size"

  val MaxRowsNumberInReport = 20
  val ColumnNumberToGenerateSimplerReportThreshold = 20
  val StringPreviewMaxLength = 300

  def report(sparkDataFrame: org.apache.spark.sql.DataFrame): Report = {
    val columnsCount = sparkDataFrame.schema.length
    if (columnsCount >= DataFrameReportGenerator.ColumnNumberToGenerateSimplerReportThreshold) {
      simplifiedReport(sparkDataFrame)
    } else {
      fullReport(sparkDataFrame)
    }
  }

  def schemaReport(sparkDataFrame: DataFrame): Report = {
    val table = schemaTable(sparkDataFrame.schema)
    Report(ReportContent(
      ReportContentName,
      ReportType.DataFrameSimplified,
      Seq(table),
      noDistributionsForSimplifiedReport(sparkDataFrame.schema)))
  }

  private def fullReport(sparkDataFrame: DataFrame): Report = {
    val multivarStats = calculateMultiColStats(sparkDataFrame)
    val distributions =
      DistributionCalculator.distributionByColumn(sparkDataFrame, multivarStats)
    val tables = Seq(
      sampleTable(sparkDataFrame),
      sizeTable(sparkDataFrame.schema, multivarStats.count)
    )
    Report(ReportContent(
      ReportContentName,
      ReportType.DataFrameFull,
      tables,
      distributions))
  }

  private def calculateMultiColStats(
    sparkDataFrame: org.apache.spark.sql.DataFrame): MultivariateStatisticalSummary = {
    val data = sparkDataFrame.rdd.map(SparkTypeConverter.rowToDoubleVector)
    Statistics.colStats(data)
  }

  private def simplifiedReport(sparkDataFrame: DataFrame): Report = {
    val tables = Seq(
      sizeTable(sparkDataFrame.schema, sparkDataFrame.count()),
      schemaTable(sparkDataFrame.schema))
    Report(ReportContent(
      ReportContentName,
      ReportType.DataFrameSimplified,
      tables,
      noDistributionsForSimplifiedReport(sparkDataFrame.schema)))
  }

  private def noDistributionsForSimplifiedReport(schema: StructType): Map[String, Distribution] = {
    for (field <- schema.fields) yield {
      field.name -> NoDistribution(field.name, NoDistributionReasons.SimplifiedReport)
    }
  }.toMap

  private def schemaTable(schema: StructType): Table = {
    val values = schema.fields.zipWithIndex.map {
      case (field, index) =>
        val columnName = field.name
        val columnType = field.dataType.simpleString
        List(Some(index.toString), Some(columnName), Some(columnType))
    }.toList

    Table(
      DataFrameReportGenerator.DataSchemaTableName,
      s"Preview of columns and their types in dataset",
      Some(List("Column index", "Column name", "Column type")),
      List(ColumnType.numeric, ColumnType.string, ColumnType.string),
      None,
      values)
  }

  private def sampleTable(sparkDataFrame: org.apache.spark.sql.DataFrame): Table = {
    val columnsNames: List[String] = sparkDataFrame.schema.fieldNames.toList
    val columnsNumber = columnsNames.size
    val rows: Array[Row] = sparkDataFrame.take(DataFrameReportGenerator.MaxRowsNumberInReport)
    val values: List[List[Option[String]]] = rows.map(row =>
      (0 until columnsNumber).map { column =>
        SparkTypeConverter.cellToString(row, column)
          .map(ReportUtils.shortenLongStrings(_, StringPreviewMaxLength))
      }.toList).toList
    val columnTypes: List[ColumnType.ColumnType] = sparkDataFrame.schema.map(
      field => SparkConversions.sparkColumnTypeToColumnType(field.dataType)
    ).toList
    Table(
      DataFrameReportGenerator.DataSampleTableName,
      s"${DataFrameReportGenerator.DataSampleTableName}. " +
        s"Randomly selected ${rows.length} rows",
      Some(columnsNames),
      columnTypes,
      None,
      values)
  }

  private def sizeTable(
      schema: StructType,
      rowsCount: Long): Table = {
    val columnsCount = schema.length
    Table(
      DataFrameReportGenerator.DataFrameSizeTableName,
      s"${DataFrameReportGenerator.DataFrameSizeTableName}. " +
        s"Number of columns and number of rows in the DataFrame.",
      Some(List("Number of columns", "Number of rows")),
      List(ColumnType.numeric, ColumnType.numeric),
      None,
      List(List(Some(columnsCount.toString), Some(rowsCount.toString))))
  }

}
