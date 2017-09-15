/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.doperations

import io.deepsense.deeplang._
import io.deepsense.deeplang.doperables.{WithTrainParameters, Scorable, Trainable}
import io.deepsense.deeplang.doperables.dataframe.DataFrame

/**
 * Operation that receives Trainable and trains it on dataframe to get trained, Scorable model.
 */
trait Trainer[T <: Trainable]
  extends DOperation2To1[T, DataFrame, Scorable]
  with WithTrainParameters {

  override val parameters = trainParameters

  override protected def _execute(
      context: ExecutionContext)(
      trainable: T, dataframe: DataFrame): Scorable = {
    trainable.train(context)(parametersForTrainable)(dataframe)
  }

  override protected def _inferKnowledge(context: InferContext)(
      trainableKnowledge: DKnowledge[T],
      dataframeKnowledge: DKnowledge[DataFrame]): DKnowledge[Scorable] = {

    DKnowledge(
      for (trainable <- trainableKnowledge.types)
      yield trainable.train.infer(context)(parametersForTrainable)(dataframeKnowledge))
  }
}
