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

import scala.reflect.runtime.universe.TypeTag

import spray.json.{JsNull, JsValue}

import io.deepsense.deeplang.DOperation.Id
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.doperables.{Estimator, Transformer}
import io.deepsense.deeplang.doperations.exceptions.TooManyPossibleTypesException
import io.deepsense.deeplang.inference.{InferContext, InferenceWarnings}
import io.deepsense.deeplang.params.DynamicParam
import io.deepsense.deeplang.{DKnowledge, DOperation2To1, ExecutionContext}

case class Fit() extends DOperation2To1[Estimator, DataFrame, Transformer] {

  override val name: String = "Fit"
  override val id: Id = "0c2ff818-977b-11e5-8994-feff819cdc9f"
  override val description: String =
    "Fits an Estimator on a DataFrame"

  val estimatorParams = new DynamicParam(
    name = "Parameters of input Estimator",
    description = "These parameters are rendered dynamically, depending on type of Estimator",
    inputPort = 0)
  setDefault(estimatorParams -> JsNull)

  def setEstimatorParams(jsValue: JsValue): this.type = set(estimatorParams -> jsValue)

  override val params = declareParams(estimatorParams)

  override val tTagTI_0: TypeTag[Estimator] = typeTag[Estimator]
  override val tTagTI_1: TypeTag[DataFrame] = typeTag[DataFrame]
  override val tTagTO_0: TypeTag[Transformer] = typeTag[Transformer]

  override protected def _execute(
      ctx: ExecutionContext)(
      estimator: Estimator, dataFrame: DataFrame): Transformer = {
    estimatorWithParams(estimator).fit(ctx)(())(dataFrame)
  }

  override protected def _inferKnowledge(
      ctx: InferContext)(
      estimatorKnowledge: DKnowledge[Estimator],
      dataFrameKnowledge: DKnowledge[DataFrame]): (DKnowledge[Transformer], InferenceWarnings) = {

    if (estimatorKnowledge.size > 1) {
      throw TooManyPossibleTypesException()
    }
    val estimator = estimatorKnowledge.single
    estimatorWithParams(estimator).fit.infer(ctx)(())(dataFrameKnowledge)
  }

  /**
   * Note that DOperation should never mutate input DOperable.
   * This method copies input estimator and sets parameters in copy.
   */
  private def estimatorWithParams(estimator: Estimator): Estimator = {
    estimator.replicate().setParamsFromJson($(estimatorParams))
  }
}
