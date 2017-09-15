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

package ai.deepsense.deeplang.doperables.multicolumn

import ai.deepsense.deeplang.ExecutionContext
import ai.deepsense.deeplang.doperables.dataframe.{DataFrame, DataFrameColumnsGetter}

trait SingleColumnTransformerUtils {

  def transformSingleColumnInPlace(
    inputColumn: String,
    dataFrame: DataFrame,
    executionContext: ExecutionContext,
    transform: (String) => DataFrame
  ): DataFrame = {
    val temporaryColumnName =
      DataFrameColumnsGetter.uniqueSuffixedColumnName(inputColumn)
    val temporaryDataFrame = transform(temporaryColumnName)
    val allColumnNames = temporaryDataFrame.sparkDataFrame.schema.map(_.name)
    val filteredColumns = allColumnNames.collect {
      case columnName if columnName == inputColumn =>
        temporaryDataFrame.sparkDataFrame(temporaryColumnName).as(inputColumn)
      case columnName if columnName != temporaryColumnName =>
        temporaryDataFrame.sparkDataFrame(columnName)
    }

    val filteredDataFrame = temporaryDataFrame.sparkDataFrame.select(filteredColumns: _*)
    DataFrame.fromSparkDataFrame(filteredDataFrame)
  }
}

object SingleColumnTransformerUtils extends SingleColumnTransformerUtils
