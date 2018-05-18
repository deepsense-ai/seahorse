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
import ai.deepsense.deeplang.doperables.Transformer
import ai.deepsense.deeplang.doperables.dataframe.DataFrame
import ai.deepsense.deeplang.doperations.layout.SmallBlockLayout2To1
import ai.deepsense.deeplang.inference.{InferContext, InferenceWarnings}
import ai.deepsense.deeplang.params.DynamicParam
import ai.deepsense.deeplang.{DKnowledge, DOperation2To1, ExecutionContext}
import ai.deepsense.models.json.graph.GraphJsonProtocol.GraphReader

case class Transform()
  extends DOperation2To1[Transformer, DataFrame, DataFrame]
    with SmallBlockLayout2To1
    with OperationDocumentation {

  override val id: Id = "643d8706-24db-4674-b5b4-10b5129251fc"
  override val name: String = "Transform"
  override val description: String =
    "Transforms a DataFrame using a Transformer"

  override val since: Version = Version(1, 0, 0)

  val transformerParams = new DynamicParam(
    name = "Parameters of input Transformer",
    description = Some("These parameters are rendered dynamically, depending on type of Transformer."),
    inputPort = 0)
  setDefault(transformerParams, JsNull)

  def getTransformerParams: JsValue = $(transformerParams)
  def setTransformerParams(jsValue: JsValue): this.type = set(transformerParams, jsValue)

  override val specificParams: Array[ai.deepsense.deeplang.params.Param[_]] = Array(transformerParams)

  override lazy val tTagTI_0: TypeTag[Transformer] = typeTag
  override lazy val tTagTI_1: TypeTag[DataFrame] = typeTag
  override lazy val tTagTO_0: TypeTag[DataFrame] = typeTag

  override protected def execute(
      transformer: Transformer,
      dataFrame: DataFrame)(
      context: ExecutionContext): DataFrame = {
    transformerWithParams(transformer, context.inferContext.graphReader).transform(context)(())(dataFrame)
  }

  override protected def inferKnowledge(
      transformerKnowledge: DKnowledge[Transformer],
      dataFrameKnowledge: DKnowledge[DataFrame])(
      context: InferContext): (DKnowledge[DataFrame], InferenceWarnings) = {

    if (transformerKnowledge.size > 1) {
      (DKnowledge(DataFrame.forInference()), InferenceWarnings.empty)
    } else {
      val transformer = transformerKnowledge.single
      transformerWithParams(transformer, context.graphReader).transform.infer(context)(())(dataFrameKnowledge)
    }
  }

  private def transformerWithParams(transformer: Transformer, graphReader: GraphReader): Transformer = {
    val transformerWithParams = transformer.replicate()
      .setParamsFromJson(getTransformerParams, graphReader, ignoreNulls = true)
    validateDynamicParams(transformerWithParams)
    transformerWithParams
  }
}
