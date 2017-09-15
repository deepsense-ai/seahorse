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

import io.deepsense.commons.types.ColumnType
import io.deepsense.commons.types.ColumnType.ColumnType
import io.deepsense.reportlib.model.{ReportContent, Table}

case class DOperableReporter(title: String, tables: List[Table] = List.empty) {

  def withParameters(
      description: String,
      parameters: (String, ColumnType, String)*): DOperableReporter = {

    val parametersTable = Table(
      "Parameters",
      description,
      Some(parameters.map(_._1).toList),
      parameters.map(_._2).toList,
      None,
      List(parameters.map(_._3).map(Some(_)).toList))

    DOperableReporter(title, tables :+ parametersTable)
  }

  def withVectorScoring(operable: VectorScoring): DOperableReporter = {
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

  def report(): Report = Report(ReportContent(title, tables))

}
