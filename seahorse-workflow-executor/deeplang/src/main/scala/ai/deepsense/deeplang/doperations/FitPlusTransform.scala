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

import scala.reflect.runtime.universe._

import spray.json.{JsNull, JsValue}

import ai.deepsense.commons.utils.Version
import ai.deepsense.deeplang.DOperation.Id
import ai.deepsense.deeplang.documentation.OperationDocumentation
import ai.deepsense.deeplang.doperables.dataframe.DataFrame
import ai.deepsense.deeplang.doperables.{Estimator, Transformer}
import ai.deepsense.deeplang.doperations.exceptions.TooManyPossibleTypesException
import ai.deepsense.deeplang.doperations.layout.SmallBlockLayout2To2
import ai.deepsense.deeplang.inference.{InferContext, InferenceWarnings}
import ai.deepsense.deeplang.params.{DynamicParam, Param}
import ai.deepsense.deeplang.{DKnowledge, DOperation2To2, ExecutionContext}
import ai.deepsense.models.json.graph.GraphJsonProtocol.GraphReader

class FitPlusTransform
  extends DOperation2To2[Estimator[Transformer], DataFrame, DataFrame, Transformer]
    with SmallBlockLayout2To2
    with OperationDocumentation {

  override val id: Id = "1cb153f1-3731-4046-a29b-5ad64fde093f"
  override val name: String = "Fit + Transform"
  override val description: String = "Fits an Estimator on a DataFrame and transforms it"

  override val since: Version = Version(1, 0, 0)

  override lazy val tTagTI_0: TypeTag[Estimator[Transformer]] = typeTag[Estimator[Transformer]]
  override lazy val tTagTI_1: TypeTag[DataFrame] = typeTag[DataFrame]
  override lazy val tTagTO_0: TypeTag[DataFrame] = typeTag[DataFrame]
  override lazy val tTagTO_1: TypeTag[Transformer] = typeTag[Transformer]

  val estimatorParams = new DynamicParam(
    name = "Parameters of input Estimator",
    description = Some("These parameters are rendered dynamically, depending on type of Estimator."),
    inputPort = 0)
  setDefault(estimatorParams -> JsNull)

  def setEstimatorParams(jsValue: JsValue): this.type = set(estimatorParams -> jsValue)
  override val specificParams: Array[Param[_]] = Array(estimatorParams)

  override protected def execute(
      estimator: Estimator[Transformer],
      dataFrame: DataFrame)(
      context: ExecutionContext): (DataFrame, Transformer) = {
    val estimatorToRun = estimatorWithParams(estimator, context.inferContext.graphReader)
    val transformer: Transformer = estimatorToRun.fit(context)(())(dataFrame)
    val transformed: DataFrame = transformer.transform(context)(())(dataFrame)
    (transformed, transformer)
  }

  override protected def inferKnowledge(
      estimatorKnowledge: DKnowledge[Estimator[Transformer]],
      inputDataFrameKnowledge: DKnowledge[DataFrame])(
      context: InferContext)
      : ((DKnowledge[DataFrame], DKnowledge[Transformer]), InferenceWarnings) = {

    val (transformerKnowledge, transformerWarnings) =
      inferTransformer(context, estimatorKnowledge, inputDataFrameKnowledge)

    val (transformedDataFrameKnowledge, transformedDataFrameWarnings) =
      inferDataFrame(context, inputDataFrameKnowledge, transformerKnowledge)

    val warningsSum: InferenceWarnings = transformerWarnings ++ transformedDataFrameWarnings
    ((transformedDataFrameKnowledge, transformerKnowledge), warningsSum)
  }

  private def estimatorWithParams(
      estimator: Estimator[Transformer],
      graphReader: GraphReader): Estimator[Transformer] = {
    val estimatorWithParams = estimator.replicate()
        .setParamsFromJson($(estimatorParams), graphReader, ignoreNulls = true)
    validateDynamicParams(estimatorWithParams)
    estimatorWithParams
  }

  private def inferDataFrame(
      context: InferContext,
      inputDataFrameKnowledge: DKnowledge[DataFrame],
      transformerKnowledge: DKnowledge[Transformer])
      : (DKnowledge[DataFrame], InferenceWarnings) = {
    val (transformedDataFrameKnowledge, transformedDataFrameWarnings) =
      transformerKnowledge.single.transform.infer(context)(())(inputDataFrameKnowledge)
    (transformedDataFrameKnowledge, transformedDataFrameWarnings)
  }

  private def inferTransformer(
      context: InferContext,
      estimatorKnowledge: DKnowledge[Estimator[Transformer]],
      inputDataFrameKnowledge: DKnowledge[DataFrame])
      : (DKnowledge[Transformer], InferenceWarnings) = {
    throwIfToManyTypes(estimatorKnowledge)
    val estimator = estimatorWithParams(estimatorKnowledge.single, context.graphReader)
    val (transformerKnowledge, transformerWarnings) =
      estimator.fit.infer(context)(())(inputDataFrameKnowledge)
    (transformerKnowledge, transformerWarnings)
  }

  private def throwIfToManyTypes(estimatorKnowledge: DKnowledge[_]): Unit = {
    if (estimatorKnowledge.size > 1) {
      throw TooManyPossibleTypesException()
    }
  }
}
