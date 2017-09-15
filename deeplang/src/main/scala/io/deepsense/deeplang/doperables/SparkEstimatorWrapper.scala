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

import scala.reflect.runtime.universe._

import org.apache.spark.ml
import org.apache.spark.sql.types.StructType

import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.params.wrappers.spark.ParamsWithSparkWrappers
import io.deepsense.deeplang.{ExecutionContext, TypeUtils}

/**
 * Wrapper for creating deeplang Estimators from spark.ml Estimators.
 * It is parametrized by model and estimator types, because these entities are tightly coupled.
 *
 * We assume that every ml.Estimator and SparkModelWrapper has a no-arg constructor.
 *
 * @tparam MD Type of wrapped ml.Model
 * @tparam E Type of wrapped ml.Estimator
 * @tparam MW Type of used model wrapper
 */
abstract class SparkEstimatorWrapper
    [MD <: ml.Model[MD], E <: ml.Estimator[MD], MW <: SparkModelWrapper[MD, E]]
    (implicit val modelWrapperTag: TypeTag[MW], implicit val estimatorTag: TypeTag[E])
  extends Estimator[MW]
  with ParamsWithSparkWrappers {

  val sparkEstimator: E = createEstimatorInstance()

  override private[deeplang] def _fit(ctx: ExecutionContext, dataFrame: DataFrame): MW = {
    val sparkParams = sparkParamMap(sparkEstimator, dataFrame.sparkDataFrame.schema)
    val sparkModel = sparkEstimator.fit(
      dataFrame.sparkDataFrame,
      sparkParams)
    createModelWrapperInstance().setModel(sparkModel).setParent(this)
  }

  override private[deeplang] def _fit_infer(maybeSchema: Option[StructType]): MW = {
    // We want to throw validation exceptions here
    validateSparkEstimatorParams(sparkEstimator, maybeSchema)
    createModelWrapperInstance().setParent(this)
  }

  def createEstimatorInstance(): E = TypeUtils.instanceOfType(estimatorTag)

  def createModelWrapperInstance(): MW = TypeUtils.instanceOfType(modelWrapperTag)
}
