/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.doperables

import io.deepsense.deeplang.{DMethod1To1, DOperable}
import io.deepsense.deeplang.doperables.dataframe.DataFrame

trait Transformation extends DOperable {
  def transform: DMethod1To1[Unit, DataFrame, DataFrame]
}
