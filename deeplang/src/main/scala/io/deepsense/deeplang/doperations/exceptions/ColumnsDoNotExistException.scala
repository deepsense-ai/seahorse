/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.doperations.exceptions

import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.parameters.ColumnSelection

case class ColumnsDoNotExistException(
    selections: Vector[ColumnSelection],
    dataFrame: DataFrame)
  extends DOperationExecutionException(
    s"One or more columns from specified selection: $selections does not exist in $dataFrame",
    None)
