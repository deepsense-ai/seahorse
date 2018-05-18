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

import scala.reflect.ClassTag
import scala.reflect.runtime.universe.TypeTag

import org.apache.spark.ml
import org.apache.spark.sql.types.StructType

import ai.deepsense.deeplang.doperables.dataframe.DataFrame
import ai.deepsense.deeplang.doperables.report.Report
import ai.deepsense.deeplang.doperables.serialization.{SerializableSparkEstimator, SerializableSparkModel}
import ai.deepsense.deeplang.doperables.spark.wrappers.params.common.{HasLabelColumnParam, HasPredictionColumnCreatorParam}
import ai.deepsense.deeplang.doperables.{Estimator, SparkEstimatorWrapper, SparkModelWrapper}
import ai.deepsense.deeplang.params.wrappers.spark.ParamsWithSparkWrappers
import ai.deepsense.deeplang.params.{Param, ParamMap}
import ai.deepsense.deeplang.{ExecutionContext, TypeUtils}

/**
  * Some spark operation assume their input was string-indexed.
  * User-experience suffers from this requirement.
  * We can work around it by wrapping estimation in `StringIndexerEstimatorWrapper`.
  * `StringIndexerEstimatorWrapper` plugs in StringIndexer before operation.
  * It also makes it transparent for clients' components
  * by reverting string indexing with labelConverter.
  */
abstract class StringIndexingEstimatorWrapper
  [M <: ml.Model[M],
   E <: ml.Estimator[M],
   MW <: SparkModelWrapper[M, E],
   SIWP <: StringIndexingWrapperModel[M, E]]
  (private var wrappedEstimator: SparkEstimatorWrapper[M, E, MW]
    with HasLabelColumnParam with HasPredictionColumnCreatorParam)
  (implicit val sparkModelClassTag: ClassTag[M],
   val modelWrapperTag: TypeTag[MW],
   val estimatorTag: TypeTag[E],
   val sparkModelTag: TypeTag[M],
   val stringIndexingWrapperModelTag: TypeTag[SIWP])
  extends Estimator[SIWP] with ParamsWithSparkWrappers  {

  final override def params: Array[Param[_]] = wrappedEstimator.params
  final override def report(extended: Boolean = true): Report = wrappedEstimator.report(extended)

  final def sparkClassCanonicalName: String =
    wrappedEstimator.serializableEstimator.sparkEstimator.getClass.getCanonicalName

  private def setWrappedEstimator(
    wrappedEstimator: SparkEstimatorWrapper[M, E, MW]
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

    val serializableSparkEstimator = new SerializableSparkEstimator[M, E](
      wrappedEstimator.sparkEstimator)

    val pipeline = StringIndexingPipeline(
      df,
      serializableSparkEstimator,
      labelColumnName,
      predictionColumnName)

    val sparkDataFrame = df.sparkDataFrame

    val paramMap = sparkParamMap(
      wrappedEstimator.sparkEstimator,
      sparkDataFrame.schema)
    val pipelineModel = pipeline.fit(sparkDataFrame, paramMap)

    val sparkModel = {
      val transformer = pipelineModel.stages.find {
        case s: SerializableSparkModel[_] =>
          sparkModelClassTag.runtimeClass.isInstance(s.sparkModel)
        case t => sparkModelClassTag.runtimeClass.isInstance(t)
      }.get
      transformer.asInstanceOf[SerializableSparkModel[M]]
    }

    val sparkModelWrapper = TypeUtils.instanceOfType(modelWrapperTag)
      .setParent(wrappedEstimator.replicate(extractParamMap()))
      .setModel(sparkModel)

    val stringIndexingModelWrapper = TypeUtils.instanceOfType(stringIndexingWrapperModelTag)
      .setPipelinedModel(pipelineModel)
      .setWrappedModel(sparkModelWrapper)

    stringIndexingModelWrapper
  }

  override private[deeplang] def _fit_infer(
     schemaOpt: Option[StructType]): SIWP = {
    validateSparkEstimatorParams(wrappedEstimator.sparkEstimator, schemaOpt)
    val model = wrappedEstimator.createModelWrapperInstance()
      .setParent(wrappedEstimator.replicate(extractParamMap()))
    TypeUtils.instanceOfType(stringIndexingWrapperModelTag).setWrappedModel(model)
  }

  private[deeplang] override def paramMap: ParamMap = wrappedEstimator.paramMap

  private[deeplang] override def defaultParamMap: ParamMap = wrappedEstimator.defaultParamMap
}
