/**
 * Copyright 2016, deepsense.io
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

package io.deepsense.deeplang.doperables.stringindexingwrapper

import org.apache.spark.ml
import org.apache.spark.ml.PipelineModel
import org.apache.spark.sql.types.StructType

import io.deepsense.deeplang.ExecutionContext
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.doperables.report.Report
import io.deepsense.deeplang.doperables.{SparkModelWrapper, Transformer}
import io.deepsense.deeplang.inference.InferContext
import io.deepsense.deeplang.params.{Param, ParamMap}

/**
  * Model wrapper adding 'string indexing' behaviour.
  *
  * Concrete models (like GBTClassificationModel) must be concrete classes (leaves in hierarchy).
  * That's why this class must be abstract.
  */
abstract class StringIndexingWrapperModel[MD <: ml.Model[MD], E <: ml.Estimator[MD]](
    private var wrappedModel: SparkModelWrapper[MD, E]) extends Transformer {

  private var pipelinedModel: PipelineModel = null

  private[stringindexingwrapper] def setPipelinedModel(
      pipelinedModel: PipelineModel): this.type = {
    this.pipelinedModel = pipelinedModel
    this
  }

  private[stringindexingwrapper] def setWrappedModel(
      wrappedModel: SparkModelWrapper[MD, E]): this.type = {
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

  override private[deeplang] def _transform(ctx: ExecutionContext, df: DataFrame): DataFrame = {
    DataFrame.fromSparkDataFrame(pipelinedModel.transform(df.sparkDataFrame))
  }

  override private[deeplang] def _transformSchema(
      schema: StructType, inferContext: InferContext): Option[StructType] =
    wrappedModel._transformSchema(schema, inferContext)

  override private[deeplang] def _transformSchema(schema: StructType): Option[StructType] =
    wrappedModel._transformSchema(schema)

  override def report: Report = wrappedModel.report

  override def params: Array[Param[_]] = wrappedModel.params

  override protected def loadTransformer(ctx: ExecutionContext, path: String): Unit = {
    val pipelineModelPath = Transformer.stringIndexerPipelineFilePath(path)
    val wrappedModelPath = Transformer.stringIndexerWrappedModelFilePath(path)
    val loadedPipelineModel = PipelineModel.load(pipelineModelPath)
    setPipelinedModel(loadedPipelineModel)
    val loadedWrappedModel = Transformer.load(ctx, wrappedModelPath)
    setWrappedModel(loadedWrappedModel.asInstanceOf[SparkModelWrapper[MD, E]])
  }

  override protected def saveTransformer(ctx: ExecutionContext, path: String): Unit = {
    val pipelineModelPath = Transformer.stringIndexerPipelineFilePath(path)
    val wrappedModelPath = Transformer.stringIndexerWrappedModelFilePath(path)
    pipelinedModel.save(pipelineModelPath)
    wrappedModel.save(ctx, wrappedModelPath)
  }
}
