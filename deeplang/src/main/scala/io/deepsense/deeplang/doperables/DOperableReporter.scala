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

package io.deepsense.deeplang.doperables

import org.apache.spark.mllib.clustering.KMeansModel
import org.apache.spark.mllib.linalg.Vector

import io.deepsense.commons.types.ColumnType
import io.deepsense.commons.types.ColumnType.ColumnType
import io.deepsense.commons.utils.DoubleUtils
import io.deepsense.deeplang.doperables.machinelearning.ModelParameters
import io.deepsense.reportlib.model.{ReportContent, Table}

case class DOperableReporter(title: String, tables: List[Table] = List.empty) {

  def withParameters(parameters: ModelParameters): DOperableReporter = {

    val reportTableRows = parameters.reportTableRows

    val parametersTable = Table(
      "Parameters",
      "",
      Some(reportTableRows.map(_._1).toList),
      reportTableRows.map(_._2).toList,
      None,
      List(reportTableRows.map(_._3).map(Some(_)).toList))

    DOperableReporter(title, tables :+ parametersTable)
  }

  def withWeights(featureColumns: Seq[String], weights: Seq[Double]): DOperableReporter = {
    val rows = featureColumns.zip(weights).map {
      case (name, weight) => List(Some(name), Some(DoubleUtils.double2String(weight)))
    }.toList

    val weightsTable = Table(
      name = "Model weights",
      description = "",
      columnNames = Some(List("Column", "Weight")),
      columnTypes = List(ColumnType.string, ColumnType.numeric),
      rowNames = None,
      values = rows)
    DOperableReporter(title, tables :+ weightsTable)
  }

  def withIntercept(interceptValue: Double): DOperableReporter = {
    val interceptTable = Table(
      name = "Intercept",
      description = "",
      columnNames = None,
      columnTypes = List(ColumnType.numeric),
      rowNames = None,
      values = List(List(Some(interceptValue.toString))))
    DOperableReporter(title, tables :+ interceptTable)
  }

  def withSupervisedScorable(operable: Scorable with HasTargetColumn): DOperableReporter = {
    val featureColumnsTable = Table(
      "Feature columns",
      "",
      Some(List("Feature columns")),
      List(ColumnType.string),
      None,
      operable.featureColumns.map(column => List(Some(column))).toList)

    val targetColumnTable = Table(
      "Target column",
      "",
      Some(List("Target column")),
      List(ColumnType.string),
      None,
      Seq(operable.targetColumn).map(column => List(Some(column))).toList)

    DOperableReporter(title, tables :+ featureColumnsTable :+ targetColumnTable)
  }

  def withUnsupervisedScorable(operable: Scorable): DOperableReporter = {
    val featureColumnsTable = Table(
      "Feature columns",
      "",
      Some(List("Feature columns")),
      List(ColumnType.string),
      None,
      operable.featureColumns.map(column => List(Some(column))).toList)

    DOperableReporter(title, tables :+ featureColumnsTable)
  }

  def withCustomTable(
      name: String,
      description: String,
      columns: (String, ColumnType, Seq[String])*): DOperableReporter = {

    val (columnNames, columnTypes, columnValues) = columns.unzip3
    val rowCount = columnValues.map(_.length).max

    val padded = columnValues.map(_.padTo(rowCount, ""))
    val rows = padded.transpose.map(_.map(Some(_)))

    val table = Table(
      name,
      description,
      Some(columnNames.toList),
      columnTypes.toList,
      None,
      rows.map(_.toList).toList)

    DOperableReporter(title, tables :+ table)
  }

  def report: Report = Report(ReportContent(title, tables))

}
