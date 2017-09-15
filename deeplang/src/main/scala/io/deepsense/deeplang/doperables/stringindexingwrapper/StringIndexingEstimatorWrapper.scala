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

import scala.reflect.ClassTag
import scala.reflect.runtime.universe.TypeTag

import org.apache.spark.ml
import org.apache.spark.sql.types.StructType

import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.doperables.report.Report
import io.deepsense.deeplang.doperables.spark.wrappers.params.common.{HasLabelColumnParam, HasPredictionColumnCreatorParam}
import io.deepsense.deeplang.doperables.{Estimator, SparkEstimatorWrapper, SparkModelWrapper}
import io.deepsense.deeplang.params.wrappers.spark.ParamsWithSparkWrappers
import io.deepsense.deeplang.params.{Param, ParamMap}
import io.deepsense.deeplang.{ExecutionContext, TypeUtils}

/**
  * Some spark operation assume their input was string-indexed.
  * User-experience suffers from this requirement.
  * We can work around it by wrapping estimation in `StringIndexerEstimatorWrapper`.
  * `StringIndexerEstimatorWrapper` plugs in StringIndexer before operation.
  * It also makes it transparent for clients' components
  * by reverting string indexing with labelConverter.
  */
abstract class StringIndexingEstimatorWrapper
  [MD <: ml.Model[MD], E <: ml.Estimator[MD],
   MW <: SparkModelWrapper[MD, E], SIWP <: StringIndexingWrapperModel[MD, E]]
  (private var wrappedEstimator: SparkEstimatorWrapper[MD, E, MW]
    with HasLabelColumnParam with HasPredictionColumnCreatorParam)
  (implicit val sparkModelClassTag: ClassTag[MD],
   val modelWrapperTag: TypeTag[MW],
   val estimatorTag: TypeTag[E],
   val sparkModelTag: TypeTag[MD],
   val stringIndexingWrapperModelTag: TypeTag[SIWP])
  extends Estimator[SIWP] with ParamsWithSparkWrappers  {

  setDefaultsFrom(wrappedEstimator)

  final override def params: Array[Param[_]] = wrappedEstimator.params
  final override def report: Report = wrappedEstimator.report

  final def sparkClassCanonicalName: String =
    wrappedEstimator.sparkEstimator.getClass.getCanonicalName

  private def setWrappedEstimator(
    wrappedEstimator: SparkEstimatorWrapper[MD, E, MW]
      with HasLabelColumnParam with HasPredictionColumnCreatorParam): this.type = {
    this.wrappedEstimator = wrappedEstimator
    this
  }

  override final def replicate(extra: ParamMap): this.type = {
    val newWrappedEstimator = wrappedEstimator.replicate(extra)
    super.replicate(extra)
      .setWrappedEstimator(newWrappedEstimator)
      .asInstanceOf[this.type]
  }

  override private[deeplang] def _fit(
      ctx: ExecutionContext, df: DataFrame): SIWP = {
    val labelColumnName = df.getColumnName($(wrappedEstimator.labelColumn))
    val predictionColumnName: String = $(wrappedEstimator.predictionColumn)

    val pipeline = StringIndexingPipeline(
      df, wrappedEstimator.sparkEstimator, labelColumnName, predictionColumnName
    )

    val sparkDataFrame = df.sparkDataFrame

    val paramMap = sparkParamMap(
      wrappedEstimator.sparkEstimator, sparkDataFrame.schema)
    val pipelineModel = pipeline.fit(sparkDataFrame, paramMap)

    val sparkModel = {
      val transformer = pipelineModel.stages.find(
        t => sparkModelClassTag.runtimeClass.isInstance(t)
      ).get
      transformer.asInstanceOf[MD]
    }

    val sparkModelWrapper = TypeUtils.instanceOfType(modelWrapperTag)
      .setModel(sparkModel).setParent(wrappedEstimator)
    val stringIndexingModelWrapper = TypeUtils.instanceOfType(stringIndexingWrapperModelTag)
      .setPipelinedModel(pipelineModel).setWrappedModel(sparkModelWrapper)

    stringIndexingModelWrapper
  }

  override private[deeplang] def _fit_infer(
     schemaOpt: Option[StructType]): SIWP = {
    validateSparkEstimatorParams(wrappedEstimator.sparkEstimator, schemaOpt)
    val model = wrappedEstimator.createModelWrapperInstance().setParent(wrappedEstimator)
    TypeUtils.instanceOfType(stringIndexingWrapperModelTag).setWrappedModel(model)
  }
}
