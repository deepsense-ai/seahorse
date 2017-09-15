/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.doperations.exceptions

import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.parameters.SingleColumnSelection

case class ColumnDoesNotExistException(
    selection: SingleColumnSelection,
    dataFrame: DataFrame) extends DOperationExecutionException(
  s"Column from specified selection: $selection does not exist in $dataFrame",
  None)
