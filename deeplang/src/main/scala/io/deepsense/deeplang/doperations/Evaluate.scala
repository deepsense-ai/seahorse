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
import io.deepsense.deeplang.doperables.{Evaluator, MetricValue}
import io.deepsense.deeplang.doperations.exceptions.TooManyPossibleTypesException
import io.deepsense.deeplang.inference.{InferContext, InferenceWarnings}
import io.deepsense.deeplang.params.DynamicParam
import io.deepsense.deeplang.{DKnowledge, DOperation2To1, ExecutionContext}

case class Evaluate() extends DOperation2To1[DataFrame, Evaluator, MetricValue] {

  override val id: Id = "a88eaf35-9061-4714-b042-ddd2049ce917"
  override val name: String = "Evaluate"
  override val description: String =
    "Evaluates a DataFrame using an Evaluator"

  val evaluatorParams = new DynamicParam(
    name = "Parameters of input Evaluator",
    description = "These parameters are rendered dynamically, depending on type of Evaluator.",
    inputPort = 1)
  setDefault(evaluatorParams, JsNull)

  def getEvaluatorParams: JsValue = $(evaluatorParams)
  def setEvaluatorParams(jsValue: JsValue): this.type = set(evaluatorParams, jsValue)

  override val params = declareParams(evaluatorParams)

  override lazy val tTagTI_0: TypeTag[DataFrame] = typeTag
  override lazy val tTagTI_1: TypeTag[Evaluator] = typeTag
  override lazy val tTagTO_0: TypeTag[MetricValue] = typeTag

  override protected def _execute(
      context: ExecutionContext)(
      dataFrame: DataFrame,
      evaluator: Evaluator): MetricValue = {
    evaluatorWithParams(evaluator).evaluate(context)(())(dataFrame)
  }

  override protected def _inferKnowledge(
      context: InferContext)(
      dataFrameKnowledge: DKnowledge[DataFrame],
      evaluatorKnowledge: DKnowledge[Evaluator]): (DKnowledge[MetricValue], InferenceWarnings) = {

    if (evaluatorKnowledge.size > 1) {
      throw TooManyPossibleTypesException()
    }
    val evaluator = evaluatorKnowledge.single
    evaluatorWithParams(evaluator).evaluate.infer(context)(())(dataFrameKnowledge)
  }

  private def evaluatorWithParams(evaluator: Evaluator): Evaluator = {
    val evaluatorWithParams = evaluator.replicate().setParamsFromJson(getEvaluatorParams)
    validateDynamicParams(evaluatorWithParams)
    evaluatorWithParams
  }

}
