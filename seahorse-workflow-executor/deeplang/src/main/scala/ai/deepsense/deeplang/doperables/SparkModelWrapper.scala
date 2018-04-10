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

import org.apache.spark.ml
import org.apache.spark.ml.param.ParamMap
import org.apache.spark.sql.types.StructType

import ai.deepsense.deeplang.ExecutionContext
import ai.deepsense.deeplang.doperables.dataframe.DataFrame
import ai.deepsense.deeplang.doperables.serialization.{ParamsSerialization, SerializableSparkModel}
import ai.deepsense.deeplang.inference.exceptions.SparkTransformSchemaException
import ai.deepsense.deeplang.params.wrappers.spark.ParamsWithSparkWrappers
import ai.deepsense.deeplang.params.{Param, ParamPair}

/**
 * Wrapper for creating deeplang Transformers from spark.ml Models.
 * It is parametrized by model and estimator types, because these entities are tightly coupled.
 *
 * Every SparkModelWrapper should have a no-arg constructor.
 *
 * @tparam M type of wrapped ml.Model
 * @tparam E type of wrapped ml.Estimator
 */
abstract class SparkModelWrapper[M <: ml.Model[M], E <: ml.Estimator[M]]
  extends Transformer
  with ParamsWithSparkWrappers {

  /**
   * Model has to be set before _transform() execution.
   * We use a mutable field because model is created by estimator dynamically.
   * Passing it to _transform by parameter is not possible without changing
   * Transformer.transform signature.
   */
  var serializableModel: SerializableSparkModel[M] = _

  /**
   * Parent EstimatorWrapper has to be set before _transformSchema() execution - estimators
   * are responsible for schema inference in models. We use a mutable field because
   * estimator instances that contain fresh parameters are created dynamically.
   * Passing an estimator to _transformSchema by parameter would require a change
   * of Transformer.transformSchema signature.
   */
  var parentEstimator: SparkEstimatorWrapper[M, E, _] = _

  def sparkModel: M = serializableModel.sparkModel

  def setModel(model: SerializableSparkModel[M]) : this.type = {
    this.serializableModel = model
    this
  }

  def setParent(estimator: SparkEstimatorWrapper[M, E, _]): this.type = {
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

  override protected def applyTransform(ctx: ExecutionContext, df: DataFrame): DataFrame =
    DataFrame.fromSparkDataFrame(
      serializableModel.transform(
        df.sparkDataFrame,
        sparkParamMap(df.sparkDataFrame.schema)))

  override protected def applyTransformSchema(schema: StructType): Option[StructType] = {
    // If parentEstimator is null then the wrapper was probably
    // created for inference purposes. This model just defines the type of model
    // it is impossible to use.
    if (parentEstimator == null) {
      None
    } else {
      // We assume that all params of model are also params of estimator
      val sparkEstimatorWithParams = parentEstimator.serializableEstimator
        .copy(parentEstimator.sparkParamMap(parentEstimator.sparkEstimator, schema))
        .copy(sparkParamMap(parentEstimator.sparkEstimator, schema))
      try {
        Some(sparkEstimatorWithParams.transformSchema(schema))
      } catch {
        case e: Exception => throw SparkTransformSchemaException(e)
      }
    }
  }

  private def sparkParamMap(schema: StructType): ParamMap =
    sparkParamMap(sparkModel, schema)

  override def replicate(extra: ai.deepsense.deeplang.params.ParamMap): this.type = {
    // If parentEstimator is null then the wrapper was probably
    // created for inference purposes. Let's just stick to nulls.
    val replicatedEstimatorWrapper: SparkEstimatorWrapper[M, E, _] =
      if (parentEstimator == null) {
        null
      } else {
        parentEstimator.replicate(extractParamMap(extra))
          .asInstanceOf[SparkEstimatorWrapper[M, E, _]]
      }
    // model might not exist (if not fitted yet)
    val modelCopy = Option(serializableModel)
      .map(m => m.copy(m.extractParamMap()).setParent(parentEstimator.serializableEstimator))
      .getOrElse(null)
      .asInstanceOf[SerializableSparkModel[M]]

    val replicated = super.replicate(extractParamMap(extra))
      .setModel(modelCopy)

    if (parentEstimator == null) {
      replicated
    } else {
      replicated.setParent(replicatedEstimatorWrapper)
    }
  }

  override def loadTransformer(ctx: ExecutionContext, path: String): this.type = {
    this
      .setParent(loadParentEstimator(ctx, path))
      .setModel(loadModel(ctx, Transformer.modelFilePath(path)))
  }

  override protected def saveTransformer(ctx: ExecutionContext, path: String): Unit = {
    saveModel(path)
    saveParentEstimator(ctx, path)
  }

  private def saveModel(path: String): Unit = {
    val modelPath = Transformer.modelFilePath(path)
    serializableModel.save(modelPath)
  }

  private def saveParentEstimator(ctx: ExecutionContext, path: String): Unit = {
    val parentEstimatorFilePath = Transformer.parentEstimatorFilePath(path)
    parentEstimator.saveObjectWithParams(ctx, parentEstimatorFilePath)
  }

  private def loadParentEstimator(
      ctx: ExecutionContext,
      path: String): SparkEstimatorWrapper[M, E, _] = {
    ParamsSerialization.load(ctx, Transformer.parentEstimatorFilePath(path))
      .asInstanceOf[SparkEstimatorWrapper[M, E, _]]
  }

  protected def loadModel(ctx: ExecutionContext, path: String): SerializableSparkModel[M]
}
