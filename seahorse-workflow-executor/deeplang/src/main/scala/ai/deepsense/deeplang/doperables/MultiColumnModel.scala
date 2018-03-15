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
import org.apache.spark.sql.types.StructType

import ai.deepsense.deeplang.ExecutionContext
import ai.deepsense.deeplang.doperables.dataframe.{DataFrame, DataFrameColumnsGetter}
import ai.deepsense.deeplang.doperables.multicolumn.HasSpecificParams
import ai.deepsense.deeplang.doperables.multicolumn.MultiColumnParams.MultiColumnInPlaceChoice
import ai.deepsense.deeplang.doperables.multicolumn.MultiColumnParams.MultiColumnInPlaceChoices.{MultiColumnNoInPlace, MultiColumnYesInPlace}
import ai.deepsense.deeplang.doperables.multicolumn.MultiColumnParams.SingleOrMultiColumnChoices.MultiColumnChoice
import ai.deepsense.deeplang.doperables.multicolumn.SingleColumnParams.SingleTransformInPlaceChoices.{NoInPlaceChoice, YesInPlaceChoice}
import ai.deepsense.deeplang.inference.exceptions.SelectedIncorrectColumnsNumber
import ai.deepsense.deeplang.params.selections.MultipleColumnSelection
import ai.deepsense.deeplang.params.wrappers.spark.ParamsWithSparkWrappers
import ai.deepsense.deeplang.params.{Param, ParamMap}

/**
 * This class is returned from an Estimator when multiple column mode was selected during
 * fit. A model created in this way can be used to transform multiple columns ONLY.
 * It holds a sequence of SingleColumnModels.
 */
abstract class MultiColumnModel[
    MD <: ml.Model[MD] { val outputCol: ml.param.Param[String] },
    E <: ml.Estimator[MD] { val outputCol: ml.param.Param[String] },
    SCW <: SparkSingleColumnModelWrapper[MD, E]]
  extends SparkModelWrapper[MD, E]
  with ParamsWithSparkWrappers
  with HasSpecificParams {

  var models: Seq[SCW] = _

  val multiColumnChoice = MultiColumnChoice()

  override lazy val params: Array[Param[_]] =
    getSpecificParams :+
      multiColumnChoice.inputColumnsParam :+
      multiColumnChoice.multiInPlaceChoiceParam

  override protected def applyTransform(ctx: ExecutionContext, df: DataFrame): DataFrame = {
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

  override protected def applyTransformSchema(schema: StructType): Option[StructType] = {
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
      .setModel(m.serializableModel)
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
