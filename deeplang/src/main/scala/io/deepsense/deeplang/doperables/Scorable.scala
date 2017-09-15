/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.doperables

import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.{DMethod1To1, DOperable}

trait Scorable extends DOperable {
  val score: DMethod1To1[Unit, DataFrame, DataFrame]
}
