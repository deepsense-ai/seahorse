/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.doperations.exceptions

import org.apache.spark.sql.types.StructType

import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.parameters.ColumnSelection

case class ColumnsDoNotExistException(
    selections: Vector[ColumnSelection],
    schema: StructType)
  extends DOperationExecutionException(
    s"One or more columns from specified selection: $selections does not exist in $schema",
    None)

object ColumnsDoNotExistException {
  def apply(
      selections: Vector[ColumnSelection],
      dataFrame: DataFrame): ColumnsDoNotExistException =
    ColumnsDoNotExistException(selections, dataFrame.sparkDataFrame.schema)
}
