/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.reportlib.model.factory

import io.deepsense.reportlib.model.Table

trait TableTestFactory {

  def testTableWithLables(
    columnNames: Option[List[String]],
    rowNames: Option[List[String]],
    values: List[List[String]]): Table = Table(
      TableTestFactory.tableName,
      TableTestFactory.tableDescription,
      columnNames,
      rowNames,
      values)

  def testEmptyTable: Table =
    Table(TableTestFactory.tableName, TableTestFactory.tableDescription, None, None, List())
}

object TableTestFactory extends TableTestFactory {
  val tableName = "table name"
  val tableDescription = "table description"
}
