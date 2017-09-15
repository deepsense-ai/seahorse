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

package io.deepsense.reportlib.model.factory

import io.deepsense.reportlib.model.Table
import io.deepsense.commons.types.ColumnType.ColumnType

trait TableTestFactory {

  def testTableWithLabels(
      columnNames: Option[List[String]],
      columnTypes: Option[List[ColumnType]],
      rowNames: Option[List[String]],
      values: List[List[Option[String]]]): Table =
    Table(
      TableTestFactory.tableName,
      TableTestFactory.tableDescription,
      columnNames,
      columnTypes,
      rowNames,
      values)

  def testEmptyTable: Table =
    Table(TableTestFactory.tableName, TableTestFactory.tableDescription, None, None, None, List())
}

object TableTestFactory extends TableTestFactory {
  val tableName = "table name"
  val tableDescription = "table description"
}
