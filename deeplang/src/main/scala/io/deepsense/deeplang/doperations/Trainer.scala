/**
 * Copyright 2015, CodiLime Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.deepsense.deeplang.doperations

import io.deepsense.deeplang._
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.doperables.{Scorable, Trainable, WithTrainParameters}
import io.deepsense.deeplang.inference.{InferContext, InferenceWarnings}

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
