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

import scala.reflect.runtime.universe.TypeTag

import org.apache.spark.ml
import org.apache.spark.sql.types.StructType

import ai.deepsense.deeplang.doperables.dataframe.{DataFrame, DataFrameColumnsGetter}
import ai.deepsense.deeplang.doperables.multicolumn.MultiColumnParams.MultiColumnInPlaceChoices.{MultiColumnNoInPlace, MultiColumnYesInPlace}
import ai.deepsense.deeplang.doperables.multicolumn.MultiColumnParams.SingleOrMultiColumnChoices.{MultiColumnChoice, SingleColumnChoice}
import ai.deepsense.deeplang.doperables.multicolumn.SingleColumnParams.SingleColumnInPlaceChoice
import ai.deepsense.deeplang.doperables.multicolumn.SingleColumnParams.SingleTransformInPlaceChoices.{NoInPlaceChoice, YesInPlaceChoice}
import ai.deepsense.deeplang.params.selections.NameSingleColumnSelection
import ai.deepsense.deeplang.params.wrappers.spark.ParamsWithSparkWrappers
import ai.deepsense.deeplang.{ExecutionContext, TypeUtils}

/**
 * SparkMultiColumnEstimatorWrapper represents an estimator that is backed up by a Spark estimator.
 * The wrapped estimator (and it's model) must operate on a single column.
 * SparkMultiColumnEstimatorWrapper allows to create (basing on a Spark estimator) an estimator that
 * is capable of working on both single columns and multiple columns. Depending on the mode it
 * returns different types of models (SingleColumnModel or MultiColumnModel). Both of the returned
 * models have to have a common ancestor ("the parent model").
 * @param ev1 Evidence that the single column model is a subclass of the parent model.
 * @param ev2 Evidence that the multi column model is a subclass of the parent model.
 * @tparam MD Spark model used in Single- and MultiColumnModel.
 * @tparam E The wrapped Spark estimator.
 * @tparam MP A common ancestor of the single and multi column models
 *            produced by the SparkMultiColumnEstimatorWrapper.
 * @tparam SMW Type of the model returned when the estimator is working on a single column.
 * @tparam EW Type of the single column estimator.
 * @tparam MMW Type of the model returned when the estimator is working on multiple columns.
 */
abstract class SparkMultiColumnEstimatorWrapper[
  MD <: ml.Model[MD]  { val outputCol: ml.param.Param[String] },
  E <: ml.Estimator[MD] { val outputCol: ml.param.Param[String] },
  MP <: Transformer,
  SMW <: SparkSingleColumnModelWrapper[MD, E] with MP,
  EW <: SparkSingleColumnEstimatorWrapper[MD, E, SMW],
  MMW <: MultiColumnModel[MD, E, SMW] with MP]
(implicit val modelWrapperTag: TypeTag[SMW],
  implicit val estimatorTag: TypeTag[E],
  implicit val modelsParentTag: TypeTag[MP],
  implicit val estimatorWrapperTag: TypeTag[EW],
  implicit val multiColumnModelTag: TypeTag[MMW],
  implicit val ev1: SMW <:< MP,
  implicit val ev2: MMW <:< MP
)
 extends MultiColumnEstimator[MP, MMW, SMW]
 with ParamsWithSparkWrappers {

  val sparkEstimatorWrapper: EW = createEstimatorWrapperInstance()

  override def handleSingleColumnChoice(
      ctx: ExecutionContext,
      df: DataFrame,
      single: SingleColumnChoice): SMW = {

    val estimator = sparkEstimatorWrapper.replicate()
      .set(sparkEstimatorWrapper.inputColumn -> single.getInputColumn)
      .setSingleInPlaceParam(single.getInPlace)

    val mw = estimator._fit(ctx, df)
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
          .setSingleInPlaceParam(multiToSingleDecoder(inputColumnName))
    }

    createMultiColumnModel()
      .setModels(models)
      .setInputColumns(multi.getMultiInputColumnSelection)
      .setInPlace(multi.getMultiInPlaceChoice)
  }


  override def handleSingleColumnChoiceInfer(
      schema: Option[StructType],
      single: SingleColumnChoice): SMW = {

    import sparkEstimatorWrapper._

    sparkEstimatorWrapper.replicate()
      .set(inputColumn -> single.getInputColumn)
      .setSingleInPlaceParam(single.getInPlace)
      ._fit_infer(schema)
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
            ._fit_infer(Some(s))
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
