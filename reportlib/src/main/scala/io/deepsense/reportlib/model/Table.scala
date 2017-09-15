/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.reportlib.model

case class Table(
  name: String,
  description: String,
  columnNames: Option[List[String]],
  rowNames: Option[List[String]],
  values: List[List[String]],
  blockType: String = Table.tableType) {
  require(blockType == Table.tableType)
}

object Table {
  val tableType = "table"
}
