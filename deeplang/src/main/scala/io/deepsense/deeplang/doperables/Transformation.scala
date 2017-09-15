/*
 *  Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.doperables

import io.deepsense.deeplang.DOperable
import io.deepsense.deeplang.doperables.dataframe.DataFrame

trait Transformation extends DOperable {
  def transform(dataFrame: DataFrame): DataFrame
}
