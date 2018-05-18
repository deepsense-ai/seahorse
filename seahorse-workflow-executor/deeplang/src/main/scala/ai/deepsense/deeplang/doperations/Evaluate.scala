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
import ai.deepsense.deeplang.doperables.{Evaluator, MetricValue}
import ai.deepsense.deeplang.doperations.exceptions.TooManyPossibleTypesException
import ai.deepsense.deeplang.doperations.layout.SmallBlockLayout2To1
import ai.deepsense.deeplang.inference.{InferContext, InferenceWarnings}
import ai.deepsense.deeplang.params.DynamicParam
import ai.deepsense.deeplang.{DKnowledge, DOperation2To1, ExecutionContext}
import ai.deepsense.models.json.graph.GraphJsonProtocol.GraphReader

case class Evaluate()
  extends DOperation2To1[Evaluator, DataFrame, MetricValue]
    with SmallBlockLayout2To1
    with OperationDocumentation {

  override val id: Id = "a88eaf35-9061-4714-b042-ddd2049ce917"
  override val name: String = "Evaluate"
  override val description: String =
    "Evaluates a DataFrame using an Evaluator"

  override val since: Version = Version(1, 0, 0)

  val evaluatorParams = new DynamicParam(
    name = "Parameters of input Evaluator",
    description = Some("These parameters are rendered dynamically, depending on type of Evaluator."),
    inputPort = 0)
  setDefault(evaluatorParams, JsNull)

  def getEvaluatorParams: JsValue = $(evaluatorParams)
  def setEvaluatorParams(jsValue: JsValue): this.type = set(evaluatorParams, jsValue)

  override val specificParams: Array[ai.deepsense.deeplang.params.Param[_]] = Array(evaluatorParams)

  override lazy val tTagTI_0: TypeTag[Evaluator] = typeTag
  override lazy val tTagTI_1: TypeTag[DataFrame] = typeTag
  override lazy val tTagTO_0: TypeTag[MetricValue] = typeTag

  override protected def execute(evaluator: Evaluator, dataFrame: DataFrame)(context: ExecutionContext): MetricValue = {
    evaluatorWithParams(evaluator, context.inferContext.graphReader).evaluate(context)(())(dataFrame)
  }

  override protected def inferKnowledge(
      evaluatorKnowledge: DKnowledge[Evaluator],
      dataFrameKnowledge: DKnowledge[DataFrame])(
      context: InferContext): (DKnowledge[MetricValue], InferenceWarnings) = {

    if (evaluatorKnowledge.size > 1) {
      throw TooManyPossibleTypesException()
    }
    val evaluator = evaluatorKnowledge.single
    evaluatorWithParams(evaluator, context.graphReader).evaluate.infer(context)(())(dataFrameKnowledge)
  }

  private def evaluatorWithParams(evaluator: Evaluator, graphReader: GraphReader): Evaluator = {
    val evaluatorWithParams = evaluator.replicate()
      .setParamsFromJson(getEvaluatorParams, graphReader, ignoreNulls = true)
    validateDynamicParams(evaluatorWithParams)
    evaluatorWithParams
  }

}
