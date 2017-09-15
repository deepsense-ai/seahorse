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
import io.deepsense.deeplang.doperables.multicolumn.MultiColumnParams.MultiColumnInPlaceChoice
import io.deepsense.deeplang.doperables.multicolumn.MultiColumnParams.MultiColumnInPlaceChoices.{MultiColumnNoInPlace, MultiColumnYesInPlace}
import io.deepsense.deeplang.doperables.multicolumn.MultiColumnParams.SingleOrMultiColumnChoices.MultiColumnChoice
import io.deepsense.deeplang.doperables.multicolumn.SingleColumnParams.SingleTransformInPlaceChoices.{NoInPlaceChoice, YesInPlaceChoice}
import io.deepsense.deeplang.inference.exceptions.SelectedIncorrectColumnsNumber
import io.deepsense.deeplang.params.{ParamMap, Param}
import io.deepsense.deeplang.params.selections.MultipleColumnSelection
import io.deepsense.deeplang.params.wrappers.spark.ParamsWithSparkWrappers

/**
 * This class is returned from an Estimator when multiple column mode was selected during
 * fit. A model created in this way can be used to transform multiple columns ONLY.
 * It holds a sequence of SingleColumnModels.
 */
abstract class MultiColumnModel[
    MD <: ml.Model[MD],
    E <: ml.Estimator[MD] { val outputCol: ml.param.Param[String] },
    SCW <: SparkSingleColumnModelWrapper[MD, E]]
  extends SparkModelWrapper[MD, E]
  with ParamsWithSparkWrappers
  with HasSpecificParams {

  var models: Seq[SCW] = _

  private val multiColumnChoice = MultiColumnChoice()

  override lazy val params: Array[Param[_]] =
    getSpecificParams :+
      multiColumnChoice.inputColumnsParam :+
      multiColumnChoice.multiInPlaceChoiceParam

  override private[deeplang] def _transform(ctx: ExecutionContext, df: DataFrame): DataFrame = {
    val inputColumnNames = df.getColumnNames($(multiColumnChoice.inputColumnsParam))

    $(multiColumnChoice.multiInPlaceChoiceParam) match {
      case MultiColumnYesInPlace() =>
        models.zip(inputColumnNames).foldLeft(df) { case (partialResult, (m, inputColumnName)) =>
          replicateWithParent(m)
            .setInputColumn(inputColumnName)
            .setSingleInPlaceParam(YesInPlaceChoice())
            ._transform(ctx, partialResult)

        }
      case no: MultiColumnNoInPlace =>
        val prefix = no.getColumnsPrefix

        models.zip(inputColumnNames).foldLeft(df){
          case (partialResult, (m, inputColumnName)) =>
            val outputColumnName =
              DataFrameColumnsGetter.prefixedColumnName(inputColumnName, prefix)
            replicateWithParent(m)
              .setInputColumn(inputColumnName)
              .setSingleInPlaceParam(NoInPlaceChoice().setOutputColumn(outputColumnName))
              ._transform(ctx, partialResult)
        }
    }
  }

  override private[deeplang] def _transformSchema(schema: StructType): Option[StructType] = {
    if (models.isEmpty) {
      None
    } else {
      val inputColumnNames =
        DataFrameColumnsGetter.getColumnNames(schema, $(multiColumnChoice.inputColumnsParam))

      if(inputColumnNames.size != models.size) {
        throw SelectedIncorrectColumnsNumber(
          $(multiColumnChoice.inputColumnsParam),
          inputColumnNames,
          models.size)
      }

      $(multiColumnChoice.multiInPlaceChoiceParam) match {
        case MultiColumnYesInPlace() =>
          models.zip(inputColumnNames).foldLeft[Option[StructType]](Some(schema)) {
            case (partialResult, (m, inputColumnName)) =>
              partialResult.flatMap {
                case s =>
                  replicateWithParent(m)
                    .setInputColumn(inputColumnName)
                    .setSingleInPlaceParam(YesInPlaceChoice())
                    ._transformSchema(s)
              }
          }

        case no: MultiColumnNoInPlace =>
          val prefix = no.getColumnsPrefix
          models.zip(inputColumnNames).foldLeft[Option[StructType]](Some(schema)) {
            case (partialResult, (m, inputColumnName)) =>
              partialResult.flatMap {
                case s =>
                  val prefixedColumnName =
                    DataFrameColumnsGetter.prefixedColumnName(inputColumnName, prefix)
                  replicateWithParent(m)
                    .setInputColumn(inputColumnName)
                    .setSingleInPlaceParam(NoInPlaceChoice().setOutputColumn(prefixedColumnName))
                    ._transformSchema(s)
              }
          }
      }
    }
  }

  override def replicate(extra: ParamMap): this.type = {
    val that = this.getClass.getConstructor().newInstance().asInstanceOf[this.type]
    copyValues(that, extractParamMap(extra))
      .setModels(models.map(_.replicate(extra)))
      .asInstanceOf[this.type]
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

  def setInPlace(choice: MultiColumnInPlaceChoice): this.type = {
    set(multiColumnChoice.multiInPlaceChoiceParam -> choice)
  }
}
