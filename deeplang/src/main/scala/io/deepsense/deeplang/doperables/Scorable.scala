/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.doperables

import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.{DMethod1To1, DOperable}

trait Scorable extends DOperable {
  /**
   * String parameter contains target column's name.
   */
  def score: DMethod1To1[String, DataFrame, DataFrame]
}
