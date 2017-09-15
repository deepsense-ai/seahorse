/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.doperations

import io.deepsense.deeplang._
import io.deepsense.deeplang.doperables.{WithTrainParameters, Scorable, Trainable}
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.inference.{InferenceWarnings, InferContext}

/**
 * Operation that receives Trainable and trains it on dataframe to get trained, Scorable model.
 */
trait Trainer[T1 <: Trainable, T2 <: Scorable]
  extends DOperation2To1[T1, DataFrame, T2]
  with WithTrainParameters {

  override val parameters = trainParameters

  override val dataFramePortIndex = 1

  override protected def _execute(
      context: ExecutionContext)(
      trainable: T1, dataframe: DataFrame): T2 = {
    trainable.train(context)(parametersForTrainable)(dataframe).asInstanceOf[T2]
  }

  override protected def _inferKnowledge(context: InferContext)(
      trainableKnowledge: DKnowledge[T1],
      dataframeKnowledge: DKnowledge[DataFrame]): (DKnowledge[T2], InferenceWarnings) = {
    val outputKnowledge = for {
      trainable <- trainableKnowledge.types
      (result, _) = trainable.train.infer(context)(parametersForTrainable)(dataframeKnowledge)
    } yield result.asInstanceOf[DKnowledge[T2]]
    (DKnowledge(outputKnowledge), InferenceWarnings.empty)
  }
}
