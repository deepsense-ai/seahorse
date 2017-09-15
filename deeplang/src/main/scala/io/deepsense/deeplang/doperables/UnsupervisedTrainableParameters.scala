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

import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.parameters._

trait UnsupervisedTrainableParameters {

  protected val dataFramePortIndex: Int

  val featureColumnsParameter = ColumnSelectorParameter(
    "Columns which are to be used as features",
    portIndex = dataFramePortIndex)

  val predictionColumnParameter = SingleColumnCreatorParameter(
    "Column name for predictions",
    default = Some("prediction"))

  protected val trainParameters = ParametersSchema(
    "feature columns" -> featureColumnsParameter,
    "prediction column" -> predictionColumnParameter)

  def featureColumnNames(dataframe: DataFrame): Seq[String] =
    dataframe.getColumnNames(featureColumnsParameter.value)

  def predictionColumnName(dataframe: DataFrame): String =
    predictionColumnParameter.value

  /**
   * Names of columns w.r.t. certain dataframe.
   * @param dataframe DataFrame that we want to use.
   * @return A tuple in form (sequence of feature column names, target column name)
   */
  def columnNames(dataframe: DataFrame): (Seq[String], String) =
    (featureColumnNames(dataframe), predictionColumnName(dataframe))
}

object UnsupervisedTrainableParameters {
  def apply(
      featureColumns: MultipleColumnSelection,
      predictionColumnName: String,
      dfPortIndex: Int = 0): UnsupervisedTrainableParameters = {

    val trainableParameters = new UnsupervisedTrainableParameters {
      override protected val dataFramePortIndex: Int = dfPortIndex
    }
    trainableParameters.featureColumnsParameter.value = featureColumns
    trainableParameters.predictionColumnParameter.value = predictionColumnName
    trainableParameters
  }
}
