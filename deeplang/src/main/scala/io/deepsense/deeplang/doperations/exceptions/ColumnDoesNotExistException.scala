/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.doperations.exceptions

import io.deepsense.deeplang.doperables.dataframe.{DataFrameMetadata, DataFrame}
import io.deepsense.deeplang.parameters.SingleColumnSelection

case class ColumnDoesNotExistException(
    selection: SingleColumnSelection,
    dataFrameMetadata: DataFrameMetadata) extends DOperationExecutionException(
  s"Column from specified selection: $selection does not exist in $dataFrameMetadata",
  None)
