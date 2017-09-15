/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.parameters.exceptions

import io.deepsense.deeplang.parameters.IndexRangeColumnSelection

case class IllegalIndexRangeColumnSelectionException(selection: IndexRangeColumnSelection)
  extends ValidationException(s"The column selection $selection is invalid. " +
    "All bounds should be set and lower bound should be less or equal upper bound.")
