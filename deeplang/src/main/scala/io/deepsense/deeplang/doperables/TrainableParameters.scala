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

trait TrainableParameters {

  protected val dataFramePortIndex: Int

  val featureColumnsParameter = ColumnSelectorParameter(
    "Columns which are to be used as features",
    portIndex = dataFramePortIndex)

  val targetColumnParameter = SingleColumnSelectorParameter(
    "Column containing observations (labels)",
    portIndex = dataFramePortIndex)

  protected val trainParameters = ParametersSchema(
    "feature columns" -> featureColumnsParameter,
    "target column" -> targetColumnParameter)

  def featureColumnNames(dataframe: DataFrame): Seq[String] =
    dataframe.getColumnNames(featureColumnsParameter.value)

  def targetColumnName(dataframe: DataFrame): String =
    dataframe.getColumnName(targetColumnParameter.value)

  /**
   * Names of columns w.r.t. certain dataframe.
   * @param dataframe DataFrame that we want to use.
   * @return A tuple in form (sequence of feature column names, target column name)
   */
  def columnNames(dataframe: DataFrame): (Seq[String], String) =
    (featureColumnNames(dataframe), targetColumnName(dataframe))
}

object TrainableParameters {
  def apply(
      featureColumns: MultipleColumnSelection,
      targetColumn: SingleColumnSelection,
      dfPortIndex: Int = 0): TrainableParameters = {

    val trainableParameters = new TrainableParameters {
      override protected val dataFramePortIndex: Int = dfPortIndex
    }
    trainableParameters.featureColumnsParameter.value = featureColumns
    trainableParameters.targetColumnParameter.value = targetColumn
    trainableParameters
  }
}
