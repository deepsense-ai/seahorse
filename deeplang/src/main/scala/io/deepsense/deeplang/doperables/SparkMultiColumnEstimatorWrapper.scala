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

import scala.reflect.runtime.universe.TypeTag

import org.apache.spark.ml
import org.apache.spark.sql.types.StructType

import io.deepsense.deeplang.doperables.dataframe.{DataFrame, DataFrameColumnsGetter}
import io.deepsense.deeplang.doperables.multicolumn.MultiColumnParams.MultiColumnInPlaceChoices.{MultiColumnNoInPlace, MultiColumnYesInPlace}
import io.deepsense.deeplang.doperables.multicolumn.MultiColumnParams.SingleOrMultiColumnChoices.{MultiColumnChoice, SingleColumnChoice}
import io.deepsense.deeplang.doperables.multicolumn.SingleColumnParams.SingleColumnInPlaceChoice
import io.deepsense.deeplang.doperables.multicolumn.SingleColumnParams.SingleTransformInPlaceChoices.{NoInPlaceChoice, YesInPlaceChoice}
import io.deepsense.deeplang.doperables.spark.wrappers.params.common.HasInputColumn
import io.deepsense.deeplang.params.selections.NameSingleColumnSelection
import io.deepsense.deeplang.params.wrappers.spark.ParamsWithSparkWrappers
import io.deepsense.deeplang.{ExecutionContext, TypeUtils}

abstract class SparkMultiColumnEstimatorWrapper[
  MD <: ml.Model[MD]  { val outputCol: ml.param.Param[String] },
  E <: ml.Estimator[MD] { val outputCol: ml.param.Param[String] },
  MW <: SparkSingleColumnModelWrapper[MD, E],
  EW <: SparkSingleColumnEstimatorWrapper[MD, E, MW],
  MMW <: MultiColumnModel[MD, E, MW]]
(implicit val modelWrapperTag: TypeTag[MW],
  implicit val estimatorTag: TypeTag[E],
  implicit val estimatorWrapperTag: TypeTag[EW],
  implicit val multiColumnModelTag: TypeTag[MMW])
 extends MultiColumnEstimator
 with ParamsWithSparkWrappers {

  val sparkEstimatorWrapper: EW = createEstimatorWrapperInstance()

  override def handleSingleColumnChoice(
      ctx: ExecutionContext,
      df: DataFrame,
      single: SingleColumnChoice): MW = {

    val estimator = sparkEstimatorWrapper.replicate()
      .set(sparkEstimatorWrapper.inputColumn -> single.getInputColumn)
      .setSingleInPlaceParam(single.getInPlace)

    val mw = estimator._fit(ctx, df).asInstanceOf[MW]
      mw.set(mw.inputColumn -> single.getInputColumn)
      .setSingleInPlaceParam(single.getInPlace)
  }


  override def handleMultiColumnChoice(
      ctx: ExecutionContext,
      df: DataFrame,
      multi: MultiColumnChoice): MMW = {
    val inputColumns = df.getColumnNames(multi.getMultiInputColumnSelection)
    val multiToSingleDecoder = multiInPlaceToSingleInPlace(multi) _

    val models = inputColumns.map {
      case inputColumnName =>
        import sparkEstimatorWrapper._
        val estimator = sparkEstimatorWrapper.replicate()
          .set(inputColumn -> NameSingleColumnSelection(inputColumnName))
          .setSingleInPlaceParam(multiToSingleDecoder(inputColumnName))
        estimator.
          _fit(ctx, df)
          .asInstanceOf[MW]
          .setSingleInPlaceParam(multiToSingleDecoder(inputColumnName))
    }

    createMultiColumnModel()
      .setModels(models)
      .setInputColumns(multi.getMultiInputColumnSelection)
      .setInPlace(multi.getMultiInPlaceChoice)
  }


  override def handleSingleColumnChoiceInfer(
      schema: Option[StructType],
      single: SingleColumnChoice): Transformer with HasInputColumn = {

    import sparkEstimatorWrapper._

    sparkEstimatorWrapper.replicate()
      .set(inputColumn -> single.getInputColumn)
      .setSingleInPlaceParam(single.getInPlace)
      ._fit_infer(schema).asInstanceOf[SparkSingleColumnModelWrapper[MD, E]]
      .setSingleInPlaceParam(single.getInPlace)
  }

  override def handleMultiColumnChoiceInfer(
      schema: Option[StructType],
     multi: MultiColumnChoice): MMW = {

    schema.map { s =>
      val inputColumns =
        DataFrameColumnsGetter.getColumnNames(s, multi.getMultiInputColumnSelection)

      val multiToSingleDecoder = multiInPlaceToSingleInPlace(multi) _

      val models = inputColumns.map {
        case inputColumnName =>
          import sparkEstimatorWrapper._
          sparkEstimatorWrapper.replicate()
            .set(inputColumn -> NameSingleColumnSelection(inputColumnName))
            .setSingleInPlaceParam(multiToSingleDecoder(inputColumnName))
            ._fit_infer(Some(s)).asInstanceOf[MW]
            .setSingleInPlaceParam(multiToSingleDecoder(inputColumnName))
      }

      createMultiColumnModel()
        .setModels(models)
        .setInputColumns(multi.getMultiInputColumnSelection)
        .setInPlace(multi.getMultiInPlaceChoice)
    }.getOrElse {
      val model = createMultiColumnModel()
        .setModels(Seq.empty)

      val inputColumnsParamValue = multi.getOrDefaultOption(multi.inputColumnsParam)
      val inPlaceParamValue = multi.getOrDefaultOption(multi.multiInPlaceChoiceParam)

      inputColumnsParamValue.map(v => model.set(model.multiColumnChoice.inputColumnsParam -> v))
      inPlaceParamValue.map(v => model.set(model.multiColumnChoice.multiInPlaceChoiceParam -> v))

      model
    }

  }

  def createEstimatorWrapperInstance(): EW = TypeUtils.instanceOfType(estimatorWrapperTag)

  def createMultiColumnModel(): MMW = TypeUtils.instanceOfType(multiColumnModelTag)


  private def multiInPlaceToSingleInPlace(multi: MultiColumnChoice)
      (inputColumnName: String): SingleColumnInPlaceChoice = {
    multi.getMultiInPlaceChoice match {
      case MultiColumnYesInPlace() => YesInPlaceChoice()
      case no: MultiColumnNoInPlace =>
        val outputColumnName =
          DataFrameColumnsGetter.prefixedColumnName(inputColumnName, no.getColumnsPrefix)
        NoInPlaceChoice().setOutputColumn(outputColumnName)
    }
  }
}
