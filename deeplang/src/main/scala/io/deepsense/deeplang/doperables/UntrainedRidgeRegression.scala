/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Witold Jedrzejewski
 */

package io.deepsense.deeplang.doperables

import io.deepsense.deeplang.{DKnowledge, InferContext, ExecutionContext, DMethod1To1}
import io.deepsense.deeplang.doperables.dataframe.DataFrame

class UntrainedRidgeRegression extends RidgeRegression with Trainable {

  override val train: DMethod1To1[String, DataFrame, Scorable] =
    new DMethod1To1[String, DataFrame, Scorable] {

    override def apply(context: ExecutionContext)(parameters: String)(t0: DataFrame): Scorable = {
      new TrainedRidgeRegression  // TODO implement
    }

    override def infer(
        context: InferContext)(
        parameters: String)(
        t0: DKnowledge[DataFrame]): DKnowledge[Scorable] = {

      DKnowledge(new TrainedRidgeRegression)
    }
  }
}
