/**
 * Copyright 2016 deepsense.ai (CodiLime, Inc)
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

package ai.deepsense.deeplang.doperables.stringindexingwrapper

import org.apache.spark.ml
import org.apache.spark.ml.PipelineModel
import org.apache.spark.sql.types.StructType

import ai.deepsense.deeplang.ExecutionContext
import ai.deepsense.deeplang.doperables.dataframe.DataFrame
import ai.deepsense.deeplang.doperables.report.Report
import ai.deepsense.deeplang.doperables.{SparkModelWrapper, Transformer}
import ai.deepsense.deeplang.inference.InferContext
import ai.deepsense.deeplang.params.{Param, ParamMap}

/**
  * Model wrapper adding 'string indexing' behaviour.
  *
  * Concrete models (like GBTClassificationModel) must be concrete classes (leaves in hierarchy).
  * That's why this class must be abstract.
  */
abstract class StringIndexingWrapperModel[M <: ml.Model[M], E <: ml.Estimator[M]](
    private var wrappedModel: SparkModelWrapper[M, E]) extends Transformer {

  private var pipelinedModel: PipelineModel = null

  private[stringindexingwrapper] def setPipelinedModel(
      pipelinedModel: PipelineModel): this.type = {
    this.pipelinedModel = pipelinedModel
    this
  }

  private[stringindexingwrapper] def setWrappedModel(
      wrappedModel: SparkModelWrapper[M, E]): this.type = {
    this.wrappedModel = wrappedModel
    this
  }

  override final def replicate(extra: ParamMap): this.type = {
    val newWrappedModel = wrappedModel.replicate(extra)
    // Assumption - spark objects underhood (and pipeline) remains the same
    super.replicate(extra)
      .setPipelinedModel(pipelinedModel)
      .setWrappedModel(newWrappedModel)
      .asInstanceOf[this.type]
  }

  override protected def applyTransform(ctx: ExecutionContext, df: DataFrame): DataFrame = {
    DataFrame.fromSparkDataFrame(pipelinedModel.transform(df.sparkDataFrame))
  }

  override protected def applyTransformSchema(
      schema: StructType, inferContext: InferContext): Option[StructType] =
    wrappedModel._transformSchema(schema, inferContext)

  override protected def applyTransformSchema(schema: StructType): Option[StructType] =
    wrappedModel._transformSchema(schema)

  override def report(extended: Boolean = true): Report = wrappedModel.report(extended)

  override def params: Array[Param[_]] = wrappedModel.params

  override protected def loadTransformer(ctx: ExecutionContext, path: String): this.type = {
    val pipelineModelPath = Transformer.stringIndexerPipelineFilePath(path)
    val wrappedModelPath = Transformer.stringIndexerWrappedModelFilePath(path)
    val loadedPipelineModel = PipelineModel.load(pipelineModelPath)
    val loadedWrappedModel = Transformer.load(ctx, wrappedModelPath)
    this
      .setPipelinedModel(loadedPipelineModel)
      .setWrappedModel(loadedWrappedModel.asInstanceOf[SparkModelWrapper[M, E]])
      .setParamsFromJson(loadedWrappedModel.paramValuesToJson, ctx.inferContext.graphReader)
  }

  override protected def saveTransformer(ctx: ExecutionContext, path: String): Unit = {
    val pipelineModelPath = Transformer.stringIndexerPipelineFilePath(path)
    val wrappedModelPath = Transformer.stringIndexerWrappedModelFilePath(path)
    pipelinedModel.save(pipelineModelPath)
    wrappedModel.save(ctx, wrappedModelPath)
  }

  private[deeplang] override def paramMap: ParamMap = wrappedModel.paramMap

  private[deeplang] override def defaultParamMap: ParamMap = wrappedModel.defaultParamMap

}
