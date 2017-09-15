/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Witold Jedrzejewski
 */

package io.deepsense.deeplang.doperables

import io.deepsense.deeplang.{ExecutionContext, DMethod1To1}
import io.deepsense.deeplang.doperables.dataframe.DataFrame

class TrainedRidgeRegression extends RidgeRegression with Scorable {

  override val score = new DMethod1To1[None.type, DataFrame, DataFrame] {

    override def apply(context: ExecutionContext)(p: None.type)(dataframe: DataFrame): DataFrame = {
      new DataFrame  // TODO implement
    }
  }
}
