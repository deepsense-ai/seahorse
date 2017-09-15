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
import org.apache.spark.sql.types.StructType

import io.deepsense.deeplang.ExecutionContext
import io.deepsense.deeplang.doperables.dataframe.{DataFrame, DataFrameColumnsGetter}
import io.deepsense.deeplang.doperables.multicolumn.HasSpecificParams
import io.deepsense.deeplang.doperables.multicolumn.MultiColumnEstimatorParams.SingleOrMultiColumnEstimatorChoices.MultiColumnEstimatorChoice
import io.deepsense.deeplang.inference.exceptions.SelectedIncorrectColumnsNumber
import io.deepsense.deeplang.params.Param
import io.deepsense.deeplang.params.selections.MultipleColumnSelection
import io.deepsense.deeplang.params.wrappers.spark.ParamsWithSparkWrappers


/**
 * This class is returned from an Estimator when multiple column mode was selected during
 * fit. A model created in this way can be used to transform multiple columns ONLY.
 * It holds a sequence of SingleColumnModels.
 */
abstract class MultiColumnModel[
    MD <: ml.Model[MD],
    E <: ml.Estimator[MD],
    SCW <: SparkSingleColumnModelWrapper[MD, E]]
  extends SparkModelWrapper[MD, E]
  with ParamsWithSparkWrappers
  with HasSpecificParams {

  var models: Seq[SCW] = _

  private val multiColumnChoice = MultiColumnEstimatorChoice()

  override lazy val params: Array[Param[_]] =
    declareParams(
      getSpecificParams :+
        multiColumnChoice.inputColumnsParam :+
        multiColumnChoice.outputColumnsPrefixParam: _*)

  override private[deeplang] def _transform(ctx: ExecutionContext, df: DataFrame): DataFrame = {
    val inputColumnNames = df.getColumnNames($(multiColumnChoice.inputColumnsParam))
    val prefix = $(multiColumnChoice.outputColumnsPrefixParam)

    models.zip(inputColumnNames).foldLeft(df){ case (partialResult, (m, inputColumnName)) =>
      replicateWithParent(m)
        .setInputColumn(inputColumnName)
        .setOutputColumn(DataFrameColumnsGetter.prefixedColumnName(inputColumnName, prefix))
        ._transform(ctx, partialResult)
    }
  }

  override private[deeplang] def _transformSchema(schema: StructType): Option[StructType] = {
    if (models.isEmpty) {
      None
    } else {
      val inputColumnNames =
        DataFrameColumnsGetter.getColumnNames(schema, $(multiColumnChoice.inputColumnsParam))
      val prefix = $(multiColumnChoice.outputColumnsPrefixParam)

      if (inputColumnNames.size == models.size) {
        models.zip(inputColumnNames).foldLeft[Option[StructType]](Some(schema)) {
          case (partialResult, (m, inputColumnName)) =>
            partialResult.flatMap {
              case s =>
                val prefixedColumnName =
                  DataFrameColumnsGetter.prefixedColumnName(inputColumnName, prefix)
                replicateWithParent(m)
                  .setInputColumn(inputColumnName)
                  .setOutputColumn(prefixedColumnName)
                  ._transformSchema(s)
            }
        }
      } else {
        throw SelectedIncorrectColumnsNumber(
          $(multiColumnChoice.inputColumnsParam),
          inputColumnNames,
          models.size)
      }
    }
  }

  private def replicateWithParent(m: SCW): SCW = {
    m.replicate()
      .setParent(m.parentEstimator)
      .setModel(m.model)
  }

  def setModels(models: Seq[SCW]): this.type = {
    this.models = models
    this
  }

  def setInputColumns(selection: MultipleColumnSelection): this.type = {
    set(multiColumnChoice.inputColumnsParam -> selection)
  }

  def setOutputColumnsPrefix(prefix: String): this.type = {
    set(multiColumnChoice.outputColumnsPrefixParam -> prefix)
  }


}
