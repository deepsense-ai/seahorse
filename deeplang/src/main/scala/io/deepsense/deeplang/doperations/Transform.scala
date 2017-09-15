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
import io.deepsense.deeplang.doperables.Transformer
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.doperations.exceptions.TooManyPossibleTypesException
import io.deepsense.deeplang.inference.{InferContext, InferenceWarnings}
import io.deepsense.deeplang.params.DynamicParam
import io.deepsense.deeplang.{DKnowledge, DOperation2To1, ExecutionContext}

case class Transform() extends DOperation2To1[Transformer, DataFrame, DataFrame] {

  override val name: String = "Transform"
  override val id: Id = "643d8706-24db-4674-b5b4-10b5129251fc"

  val transformerParams = new DynamicParam(
    name = "Parameters of input Transformer",
    description = "These parameters are rendered dynamically, depending on type of Transformer",
    inputPort = 0)
  setDefault(transformerParams, JsNull)

  def getTransformerParams: JsValue = $(transformerParams)
  def setTransformerParams(jsValue: JsValue): this.type = set(transformerParams, jsValue)

  override val params = declareParams(transformerParams)

  override val tTagTI_0: TypeTag[Transformer] = typeTag[Transformer]
  override val tTagTI_1: TypeTag[DataFrame] = typeTag[DataFrame]
  override val tTagTO_0: TypeTag[DataFrame] = typeTag[DataFrame]

  override protected def _execute(
      context: ExecutionContext)(
      transformer: Transformer,
      dataFrame: DataFrame): DataFrame = {
    transformerWithParams(transformer).transform(context)(())(dataFrame)
  }

  override protected def _inferKnowledge(
      context: InferContext)(
      transformerKnowledge: DKnowledge[Transformer],
      dataFrameKnowledge: DKnowledge[DataFrame]): (DKnowledge[DataFrame], InferenceWarnings) = {

    if (transformerKnowledge.size > 1) {
      throw TooManyPossibleTypesException()
    }
    val transformer = transformerKnowledge.single
    transformerWithParams(transformer).transform.infer(context)(())(dataFrameKnowledge)
  }

  private def transformerWithParams(transformer: Transformer): Transformer =
    transformer.replicate().setParamsFromJson(getTransformerParams)
}
