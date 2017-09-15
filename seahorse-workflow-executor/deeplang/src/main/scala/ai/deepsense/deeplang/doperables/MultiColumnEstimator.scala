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

import scala.language.reflectiveCalls
import scala.reflect.runtime.universe.TypeTag

import org.apache.spark.sql.types.StructType

import ai.deepsense.deeplang.ExecutionContext
import ai.deepsense.deeplang.doperables.dataframe.DataFrame
import ai.deepsense.deeplang.doperables.multicolumn.HasSpecificParams
import ai.deepsense.deeplang.doperables.multicolumn.MultiColumnParams.MultiColumnInPlaceChoices.{MultiColumnNoInPlace, MultiColumnYesInPlace}
import ai.deepsense.deeplang.doperables.multicolumn.MultiColumnParams.SingleOrMultiColumnChoices.{MultiColumnChoice, SingleColumnChoice}
import ai.deepsense.deeplang.doperables.multicolumn.SingleColumnParams.SingleTransformInPlaceChoices.{NoInPlaceChoice, YesInPlaceChoice}
import ai.deepsense.deeplang.doperables.spark.wrappers.params.common.HasInputColumn
import ai.deepsense.deeplang.params.IOColumnsParam
import ai.deepsense.deeplang.params.selections.NameSingleColumnSelection

/**
 * MultiColumnEstimator is a [[ai.deepsense.deeplang.doperables.Estimator]]
 * that can work on either a single column or multiple columns.
 * Also, it can also work in-place (by replacing columns) or
 * not (new columns will be appended to a [[ai.deepsense.deeplang.doperables.dataframe.DataFrame]]).
 *
 * @tparam T Parent type of the returned transformers.
 * @tparam MC The type of the returned transformer when working on multiple columns.
 * @tparam SC The type of the returned transformer when working on a single column.
 */
abstract class MultiColumnEstimator[T <: Transformer, MC <: T, SC <: T with HasInputColumn]
(implicit val transformerTypeTag: TypeTag[T])
  extends Estimator[T] with HasSpecificParams {

  val singleOrMultiChoiceParam = IOColumnsParam()
  override lazy val params = getSpecificParams :+ singleOrMultiChoiceParam

  def setSingleColumn(inputColumnName: String, outputColumnName: String): this.type = {
    val choice = SingleColumnChoice()
      .setInPlace(NoInPlaceChoice().setOutputColumn(outputColumnName))
      .setInputColumn(NameSingleColumnSelection(inputColumnName))
    set(singleOrMultiChoiceParam, choice)
  }

  def setSingleColumnInPlace(inputColumnName: String): this.type = {
    val choice = SingleColumnChoice()
      .setInPlace(YesInPlaceChoice())
      .setInputColumn(NameSingleColumnSelection(inputColumnName))
    set(singleOrMultiChoiceParam, choice)
  }

  def setMultipleColumn(inputColumnNames: Set[String], outputColumnPrefix: String): this.type = {
    val choice = MultiColumnChoice(inputColumnNames)
      .setMultiInPlaceChoice(MultiColumnNoInPlace().setColumnsPrefix(outputColumnPrefix))
    set(singleOrMultiChoiceParam, choice)
  }

  def setMultipleColumnInPlace(inputColumnNames: Set[String]): this.type = {
    val choice = MultiColumnChoice(inputColumnNames)
      .setMultiInPlaceChoice(MultiColumnYesInPlace())
    set(singleOrMultiChoiceParam, choice)
  }

  def handleSingleColumnChoice(
    ctx: ExecutionContext,
    df: DataFrame,
    single: SingleColumnChoice): SC

  def handleMultiColumnChoice(
    ctx: ExecutionContext,
    df: DataFrame,
    multi: MultiColumnChoice): MC

  /**
   * Creates a Transformer based on a DataFrame.
   */
  override private[deeplang] def _fit(ctx: ExecutionContext, df: DataFrame): T = {
    $(singleOrMultiChoiceParam) match {
      case single: SingleColumnChoice =>
        handleSingleColumnChoice(ctx, df, single)
      case multi: MultiColumnChoice =>
        handleMultiColumnChoice(ctx, df, multi)
    }
  }

  def handleSingleColumnChoiceInfer(
    schema: Option[StructType],
    single: SingleColumnChoice): SC

  def handleMultiColumnChoiceInfer(
    schema: Option[StructType],
    multi: MultiColumnChoice): MC

  /**
   * Creates an instance of Transformer for inference.
   * @param schema the schema for inference, or None if it's unknown.
   */
  override private[deeplang] def _fit_infer(
      schema: Option[StructType]): T = {
    $(singleOrMultiChoiceParam) match {
      case single: SingleColumnChoice =>
        handleSingleColumnChoiceInfer(schema, single)
      case multi: MultiColumnChoice =>
        handleMultiColumnChoiceInfer(schema, multi)
    }
  }
}





