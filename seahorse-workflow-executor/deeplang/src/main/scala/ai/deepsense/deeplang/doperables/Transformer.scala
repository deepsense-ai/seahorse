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

package ai.deepsense.deeplang.doperables

import org.apache.spark.SparkException
import org.apache.spark.sql.types.StructType

import ai.deepsense.commons.utils.Logging
import ai.deepsense.deeplang._
import ai.deepsense.deeplang.doperables.dataframe.DataFrame
import ai.deepsense.deeplang.doperables.report.{CommonTablesGenerators, Report}
import ai.deepsense.deeplang.doperables.serialization.{Loadable, ParamsSerialization, PathsUtils}
import ai.deepsense.deeplang.doperations.exceptions.WriteFileException
import ai.deepsense.deeplang.inference.{InferContext, InferenceWarnings}
import ai.deepsense.deeplang.params.Params
import ai.deepsense.reportlib.model.ReportType

/**
 * Able to transform a DataFrame into another DataFrame.
 * Can have mutable parameters.
 */
abstract class Transformer
    extends DOperable
    with Params
    with Logging
    with ParamsSerialization
    with Loadable {

  /**
   * Creates a transformed DataFrame based on input DataFrame.
   */
  protected def applyTransform(ctx: ExecutionContext, df: DataFrame): DataFrame

  /**
   * Library internal direct access to `applyTransform`.
   */
  private[deeplang] final def _transform(ctx: ExecutionContext, df: DataFrame): DataFrame = applyTransform(ctx, df)

  /**
   * Should be implemented in subclasses.
   * For known schema of input DataFrame, infers schema of output DataFrame.
   * If it is not able to do it for some reasons, it returns None.
   */
  protected def applyTransformSchema(schema: StructType): Option[StructType] = None

  /**
    * Library internal direct access to `applyTransformSchema`.
    */
  private[deeplang] final def _transformSchema(schema: StructType): Option[StructType] = applyTransformSchema(schema)

  /**
    * Can be implemented in a subclass, if access to infer context is required.
    * For known schema of input DataFrame, infers schema of output DataFrame.
    * If it is not able to do it for some reasons, it returns None.
    */
  protected def applyTransformSchema(
      schema: StructType,
      inferContext: InferContext): Option[StructType] = applyTransformSchema(schema)

  /**
    * Library internal direct access to `applyTransformSchema`.
    */
  private[deeplang] final def _transformSchema(
    schema: StructType,
    inferContext: InferContext): Option[StructType] = applyTransformSchema(schema, inferContext)

  def transform: DMethod1To1[Unit, DataFrame, DataFrame] = {
    new DMethod1To1[Unit, DataFrame, DataFrame] {
      override def apply(ctx: ExecutionContext)(p: Unit)(df: DataFrame): DataFrame = {
        applyTransform(ctx, df)
      }

      override def infer(
        ctx: InferContext)(
        p: Unit)(
        k: DKnowledge[DataFrame]): (DKnowledge[DataFrame], InferenceWarnings) = {
        val df = DataFrame.forInference(k.single.schema.flatMap(s => applyTransformSchema(s, ctx)))
        (DKnowledge(df), InferenceWarnings.empty)
      }
    }
  }

  override def report(extended: Boolean = true): Report =
    super.report(extended)
      .withReportName(s"$transformerName Report")
      .withReportType(ReportType.Model)
      .withAdditionalTable(CommonTablesGenerators.params(extractParamMap()))

  protected def transformerName: String = this.getClass.getSimpleName

  def save(ctx: ExecutionContext, path: String): Unit = {
    try {
      saveObjectWithParams(ctx, path)
      saveTransformer(ctx, path)
    } catch {
      case e: SparkException =>
        logger.error(s"Saving Transformer error: Spark problem. Unable to write file to $path", e)
        throw WriteFileException(path, e)
    }
  }

  override def load(ctx: ExecutionContext, path: String): this.type = {
    loadTransformer(ctx, path).loadAndSetParams(ctx, path)
  }

  protected def saveTransformer(ctx: ExecutionContext, path: String): Unit = {}

  protected def loadTransformer(ctx: ExecutionContext, path: String): this.type = {
    this
  }
}

object Transformer extends Logging {

  private val modelFilePath = "deepsenseModel"
  private val transformerFilePath = "deepsenseTransformer"
  private val parentEstimatorFilePath = "deepsenseParentEstimator"
  private val pipelineFilePath = "deepsensePipeline"
  private val wrappedModelFilePath = "deepsenseWrappedModel"

  def load(ctx: ExecutionContext, path: String): Transformer = {
    logger.debug("Loading transformer from: {}", path)
    ParamsSerialization.load(ctx, path).asInstanceOf[Transformer]
  }

  def modelFilePath(path: String): String = {
    PathsUtils.combinePaths(path, modelFilePath)
  }

  def parentEstimatorFilePath(path: String): String = {
    PathsUtils.combinePaths(path, parentEstimatorFilePath)
  }

  def stringIndexerPipelineFilePath(path: String): String = {
    PathsUtils.combinePaths(modelFilePath(path), pipelineFilePath)
  }

  def stringIndexerWrappedModelFilePath(path: String): String = {
    PathsUtils.combinePaths(modelFilePath(path), wrappedModelFilePath)
  }

  def transformerSparkTransformerFilePath(path: String): String = {
    PathsUtils.combinePaths(path, transformerFilePath)
  }
}
