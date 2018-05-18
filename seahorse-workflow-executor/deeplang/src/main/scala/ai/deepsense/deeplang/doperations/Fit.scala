/**
 * Copyright 2015 deepsense.ai (CodiLime, Inc)
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

package ai.deepsense.deeplang.doperations

import scala.reflect.runtime.universe.TypeTag

import spray.json.{JsNull, JsValue}

import ai.deepsense.commons.utils.Version
import ai.deepsense.deeplang.DOperation.Id
import ai.deepsense.deeplang.documentation.OperationDocumentation
import ai.deepsense.deeplang.doperables.dataframe.DataFrame
import ai.deepsense.deeplang.doperables.{Estimator, Transformer}
import ai.deepsense.deeplang.doperations.exceptions.TooManyPossibleTypesException
import ai.deepsense.deeplang.doperations.layout.SmallBlockLayout2To1
import ai.deepsense.deeplang.inference.{InferContext, InferenceWarnings}
import ai.deepsense.deeplang.params.DynamicParam
import ai.deepsense.deeplang.{DKnowledge, DOperation2To1, ExecutionContext}
import ai.deepsense.models.json.graph.GraphJsonProtocol.GraphReader

case class Fit()
  extends DOperation2To1[Estimator[Transformer], DataFrame, Transformer]
    with SmallBlockLayout2To1
    with OperationDocumentation {

  override val id: Id = "0c2ff818-977b-11e5-8994-feff819cdc9f"
  override val name: String = "Fit"
  override val description: String =
    "Fits an Estimator on a DataFrame"

  override val since: Version = Version(1, 0, 0)

  val estimatorParams = new DynamicParam(
    name = "Parameters of input Estimator",
    description = Some("These parameters are rendered dynamically, depending on type of Estimator."),
    inputPort = 0)
  setDefault(estimatorParams -> JsNull)

  def setEstimatorParams(jsValue: JsValue): this.type = set(estimatorParams -> jsValue)

  override val specificParams: Array[ai.deepsense.deeplang.params.Param[_]] = Array(estimatorParams)

  override lazy val tTagTI_0: TypeTag[Estimator[Transformer]] = typeTag
  override lazy val tTagTI_1: TypeTag[DataFrame] = typeTag
  override lazy val tTagTO_0: TypeTag[Transformer] = typeTag

  override protected def execute(
      estimator: Estimator[Transformer],
      dataFrame: DataFrame)(
      ctx: ExecutionContext): Transformer = {
    estimatorWithParams(estimator, ctx.inferContext.graphReader).fit(ctx)(())(dataFrame)
  }

  override protected def inferKnowledge(
      estimatorKnowledge: DKnowledge[Estimator[Transformer]],
      dataFrameKnowledge: DKnowledge[DataFrame])(
      ctx: InferContext)
    : (DKnowledge[Transformer], InferenceWarnings) = {

    if (estimatorKnowledge.size > 1) {
      throw TooManyPossibleTypesException()
    }
    val estimator = estimatorKnowledge.single
    estimatorWithParams(estimator, ctx.graphReader).fit.infer(ctx)(())(dataFrameKnowledge)
  }

  /**
   * Note that DOperation should never mutate input DOperable.
   * This method copies input estimator and sets parameters in copy.
   */
  private def estimatorWithParams(estimator: Estimator[Transformer], graphReader: GraphReader):
  Estimator[Transformer] = {
    val estimatorWithParams = estimator.replicate()
      .setParamsFromJson($(estimatorParams), graphReader, ignoreNulls = true)
    validateDynamicParams(estimatorWithParams)
    estimatorWithParams
  }
}
