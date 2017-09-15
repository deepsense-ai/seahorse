/**
 * Copyright 2015, deepsense.io
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

import io.deepsense.deeplang.doperables.Scorable
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.inference.{InferContext, InferenceWarnings}
import io.deepsense.deeplang.parameters.{ParametersSchema, SingleColumnCreatorParameter}
import io.deepsense.deeplang.{DKnowledge, DOperation2To1, ExecutionContext}

trait Scorer[T <: Scorable] extends DOperation2To1[T, DataFrame, DataFrame] {

  val predictionColumnParam = SingleColumnCreatorParameter(
    "Column name for predictions",
    Some("prediction"),
    required = true)

  override val parameters = ParametersSchema(
    "prediction column" -> predictionColumnParam
  )

  override protected def _execute(
      context: ExecutionContext)(
      scorable: T, dataframe: DataFrame): DataFrame = {
    scorable.score(context)(predictionColumnParam.value.get)(dataframe)
  }

  override protected def _inferFullKnowledge(
      context: InferContext)(
      scorableKnowledge: DKnowledge[T], dataFrameKnowledge: DKnowledge[DataFrame])
      : (DKnowledge[DataFrame], InferenceWarnings) = {
    val inferenceResults = for (scorable <- scorableKnowledge.types)
      yield scorable.score.infer(context)(predictionColumnParam.value.get)(dataFrameKnowledge)
    val (inferredDataFrameKnowledge, inferenceWarnings) = inferenceResults.unzip
    (DKnowledge(inferredDataFrameKnowledge), InferenceWarnings.flatten(inferenceWarnings.toVector))
  }
}
