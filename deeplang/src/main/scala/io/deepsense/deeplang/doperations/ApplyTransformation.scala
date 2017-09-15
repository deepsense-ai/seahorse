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

import io.deepsense.deeplang.DOperation.Id
import io.deepsense.deeplang.doperables.Transformation
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.inference.{InferenceWarnings, InferContext}
import io.deepsense.deeplang.parameters.ParametersSchema
import io.deepsense.deeplang.{DKnowledge, DOperation2To1, ExecutionContext}

case class ApplyTransformation() extends DOperation2To1[Transformation, DataFrame, DataFrame] {
  override val parameters: ParametersSchema = ParametersSchema()
  override val name: String = "Apply Transformation"
  override val id: Id = "f6e1f59b-d04d-44e2-ae35-2fcada44d23f"

  override protected def _execute(
      context: ExecutionContext)(transformation: Transformation, dataFrame: DataFrame): DataFrame =
    transformation.transform(context)(())(dataFrame)

  override protected def _inferFullKnowledge(
      context: InferContext)(
      transformationKnowledge: DKnowledge[Transformation],
      dataFrameKnowledge: DKnowledge[DataFrame])
      : (DKnowledge[DataFrame], InferenceWarnings) = {
    val inferenceResults = for (transformation <- transformationKnowledge.types)
    yield transformation.transform.infer(context)(())(dataFrameKnowledge)
    val (inferredDataFrameKnowledge, inferenceWarnings) = inferenceResults.unzip
    (DKnowledge(inferredDataFrameKnowledge), InferenceWarnings.flatten(inferenceWarnings.toVector))
  }
}
