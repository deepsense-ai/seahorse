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

import scala.language.reflectiveCalls

import org.apache.spark.sql.types.StructType

import io.deepsense.deeplang.ExecutionContext
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.doperables.multicolumn.HasSpecificParams
import io.deepsense.deeplang.doperables.multicolumn.MultiColumnParams.MultiColumnInPlaceChoices.MultiColumnNoInPlace
import io.deepsense.deeplang.doperables.multicolumn.MultiColumnParams.SingleOrMultiColumnChoice
import io.deepsense.deeplang.doperables.multicolumn.MultiColumnParams.SingleOrMultiColumnChoices.{MultiColumnChoice, SingleColumnChoice}
import io.deepsense.deeplang.doperables.multicolumn.SingleColumnParams.SingleTransformInPlaceChoices.NoInPlaceChoice
import io.deepsense.deeplang.doperables.spark.wrappers.params.common.HasInputColumn
import io.deepsense.deeplang.params.IOColumnsParam
import io.deepsense.deeplang.params.choice.ChoiceParam
import io.deepsense.deeplang.params.selections.{MultipleColumnSelection, NameColumnSelection, NameSingleColumnSelection}

/**
 * MultiColumnEstimator is a [[io.deepsense.deeplang.doperables.Estimator]]
 * that can work on either a single column or multiple columns.
 * Also, it can also work in-place (by replacing columns) or
 * not (new columns will be appended to a [[io.deepsense.deeplang.doperables.dataframe.DataFrame]]).
 * When not working in-place and when working with a single column one has to
 * specify output column's name.
 * When working with multiple columns one has to specify output column names' prefix.
 *
 * In single-column mode, a MultiColumnTransformer will be returned.
 * In multi-column mode, an AlwaysMultiColumnTransformer will be returned.
 */
abstract class MultiColumnEstimator extends Estimator with HasSpecificParams {

  val singleOrMultiChoiceParam = IOColumnsParam()
  override lazy val params = getSpecificParams :+ singleOrMultiChoiceParam

  def setSingleColumn(inputColumnName: String, outputColumnName: String): this.type = {
    val choice = SingleColumnChoice()
      .setInPlace(NoInPlaceChoice().setOutputColumn(outputColumnName))
      .setInputColumn(NameSingleColumnSelection(inputColumnName))
    set(singleOrMultiChoiceParam, choice)
  }

  def setMultipleColumn(inputColumnNames: Set[String], outputColumnPrefix: String): this.type = {
    val choice = MultiColumnChoice()
      .setMultiInPlaceChoice(MultiColumnNoInPlace().setColumnsPrefix(outputColumnPrefix))
      .setInputColumnsParam(MultipleColumnSelection(Vector(NameColumnSelection(inputColumnNames))))
    set(singleOrMultiChoiceParam, choice)
  }

  def handleSingleColumnChoice(
    ctx: ExecutionContext,
    df: DataFrame,
    single: SingleColumnChoice): Transformer with HasInputColumn

  def handleMultiColumnChoice(
    ctx: ExecutionContext,
    df: DataFrame,
    multi: MultiColumnChoice): Transformer

  /**
   * Creates a Transformer based on a DataFrame.
   */
  override private[deeplang] def _fit(ctx: ExecutionContext, df: DataFrame): Transformer = {
    $(singleOrMultiChoiceParam) match {
      case single: SingleColumnChoice =>
        handleSingleColumnChoice(ctx, df, single)
      case multi: MultiColumnChoice =>
        handleMultiColumnChoice(ctx, df, multi)
    }
  }

  def handleSingleColumnChoiceInfer(
    schema: Option[StructType],
    single: SingleColumnChoice): Transformer with HasInputColumn

  def handleMultiColumnChoiceInfer(
    schema: Option[StructType],
    multi: MultiColumnChoice): Transformer

  /**
   * Creates an instance of Transformer for inference.
   * @param schema the schema for inference, or None if it's unknown.
   */
  override private[deeplang] def _fit_infer(schema: Option[StructType]): Transformer = {
    $(singleOrMultiChoiceParam) match {
      case single: SingleColumnChoice =>
        handleSingleColumnChoiceInfer(schema, single)
      case multi: MultiColumnChoice =>
        handleMultiColumnChoiceInfer(schema, multi)
    }
  }
}





