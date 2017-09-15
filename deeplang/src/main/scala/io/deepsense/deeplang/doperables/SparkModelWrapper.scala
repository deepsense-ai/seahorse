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

package io.deepsense.deeplang.doperables

import org.apache.spark.ml
import org.apache.spark.ml.param.ParamMap
import org.apache.spark.ml.util.MLWritable
import org.apache.spark.sql.types.StructType

import io.deepsense.deeplang.ExecutionContext
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.doperables.serialization.{CustomPersistence, ParamsSerialization}
import io.deepsense.deeplang.inference.exceptions.SparkTransformSchemaException
import io.deepsense.deeplang.params.wrappers.spark.ParamsWithSparkWrappers
import io.deepsense.deeplang.params.{Param, ParamPair}

/**
 * Wrapper for creating deeplang Transformers from spark.ml Models.
 * It is parametrized by model and estimator types, because these entities are tightly coupled.
 *
 * Every SparkModelWrapper should have a no-arg constructor.
 *
 * @tparam MD type of wrapped ml.Model
 * @tparam E type of wrapped ml.Estimator
 */
abstract class SparkModelWrapper[
    MD <: ml.Model[MD], E <: ml.Estimator[MD]]
  extends Transformer
  with ParamsWithSparkWrappers {

  /**
   * Model has to be set before _transform() execution.
   * We use a mutable field because model is created by estimator dynamically.
   * Passing it to _transform by parameter is not possible without changing
   * Transformer.transform signature.
   */
  var model: MD = _

  /**
   * Parent EstimatorWrapper has to be set before _transformSchema() execution - estimators
   * are responsible for schema inference in models. We use a mutable field because
   * estimator instances that contain fresh parameters are created dynamically.
   * Passing an estimator to _transformSchema by parameter would require a change
   * of Transformer.transformSchema signature.
   */
  var parentEstimator: SparkEstimatorWrapper[MD, E, _] = _

  def setModel(model: MD) : this.type = {
    this.model = model
    this
  }

  def setParent(estimator: SparkEstimatorWrapper[MD, E, _]): this.type = {
    parentEstimator = estimator
    // Model wrapper should inherit parameter values from Estimator wrapper
    this.set(parentEstimator.extractParamMap())
  }

  /**
    * Spark model wrappers mustn't have default parameters.
    * All initial parameter values are inherited from parent estimator.
    */
  override protected def setDefault[T](param: Param[T], value: T): this.type = this

  override protected def setDefault(paramPairs: ParamPair[_]*): this.type = this

  override private[deeplang] def _transform(ctx: ExecutionContext, df: DataFrame): DataFrame =
    DataFrame.fromSparkDataFrame(
      model.transform(
        df.sparkDataFrame,
        sparkParamMap(df.sparkDataFrame.schema)))

  override private[deeplang] def _transformSchema(schema: StructType): Option[StructType] = {
    // We assume that all params of model are also params of estimator
    val sparkEstimatorWithParams = parentEstimator.sparkEstimator
      .copy(parentEstimator.sparkParamMap(parentEstimator.sparkEstimator, schema))
      .copy(sparkParamMap(parentEstimator.sparkEstimator, schema))
    try {
      Some(sparkEstimatorWithParams.transformSchema(schema))
    } catch {
      case e: Exception => throw SparkTransformSchemaException(e)
    }
  }

  private def sparkParamMap(schema: StructType): ParamMap = sparkParamMap(model, schema)

  override def replicate(extra: io.deepsense.deeplang.params.ParamMap): this.type = {
    val replicatedEstimatorWrapper: SparkEstimatorWrapper[MD, E, _] =
      parentEstimator.replicate(extractParamMap(extra))
        .asInstanceOf[SparkEstimatorWrapper[MD, E, _]]

    // model might not exist (if not fitted yet)
    val modelCopy = Option(model)
      .map(m => m.copy(m.extractParamMap()).setParent(parentEstimator.sparkEstimator))
      .getOrElse(null)
      .asInstanceOf[MD]

    super.replicate(extractParamMap(extra))
      .setParent(replicatedEstimatorWrapper)
      .setModel(modelCopy)
  }

  override def loadTransformer(ctx: ExecutionContext, path: String): this.type = {
    this
      .setParent(loadParentEstimator(ctx, path))
      .setModel(loadModel(ctx, Transformer.modelFilePath(path)))
  }

  override protected def saveTransformer(ctx: ExecutionContext, path: String): Unit = {
    val modelPath = Transformer.modelFilePath(path)
    model match {
      case writable: MLWritable =>
        saveModel(modelPath, writable)
      case _ =>
        CustomPersistence.save(ctx.sparkContext, model, modelPath)
    }
    saveParentEstimator(ctx, path)
  }

  private def saveModel(modelPath: String, writable: MLWritable): Unit = {
    writable.save(modelPath)
  }

  private def saveParentEstimator(ctx: ExecutionContext, path: String): Unit = {
    val parentEstimatorFilePath = Transformer.parentEstimatorFilePath(path)
    parentEstimator.saveObjectWithParams(ctx, parentEstimatorFilePath)
  }

  private def loadParentEstimator(
    ctx: ExecutionContext,
    path: String): SparkEstimatorWrapper[MD, E, _] = {
    ParamsSerialization.load(ctx, Transformer.parentEstimatorFilePath(path))
      .asInstanceOf[SparkEstimatorWrapper[MD, E, _]]
  }

  protected def loadModel(ctx: ExecutionContext, path: String): MD
}
