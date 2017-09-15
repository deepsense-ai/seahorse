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

package io.deepsense.deeplang

import io.deepsense.deeplang.catalogs.doperable.DOperableCatalog
import io.deepsense.deeplang.catalogs.doperations.DOperationsCatalog
import io.deepsense.deeplang.doperables._
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.doperations._

/**
 * Object used to register all desired DOperables and DOperations.
 */
object CatalogRecorder {

  def registerDOperables(catalog: DOperableCatalog): Unit = {
    catalog.registerDOperable[DataFrame]()
    catalog.registerDOperable[Report]()

    catalog.registerDOperable[ColumnsFilterer]()
    catalog.registerDOperable[DatetimeDecomposer]()
    catalog.registerDOperable[MathematicalTransformation]()
    catalog.registerDOperable[MissingValuesHandler]()
    catalog.registerDOperable[SqlExpression]()
    catalog.registerDOperable[TypeConverter]()
  }

  def registerDOperations(catalog: DOperationsCatalog): Unit = {

    catalog.registerDOperation[ReadDataFrame](
      DOperationCategories.IO,
      "Reads a DataFrame from a file or database"
    )

    catalog.registerDOperation[WriteDataFrame](
      DOperationCategories.IO,
      "Writes a DataFrame to a file or database"
    )

    catalog.registerDOperation[ConvertType](
      DOperationCategories.Transformation,
      "Converts selected columns of a DataFrame to a different type"
    )

    catalog.registerDOperation[DecomposeDatetime](
      DOperationCategories.Transformation,
      "Extracts Numeric fields (year, month, etc.) from a Timestamp column"
    )

    catalog.registerDOperation[ExecuteMathematicalTransformation](
      DOperationCategories.Transformation,
      "Executes a mathematical transformation on a column of a DataFrame"
    )

    catalog.registerDOperation[ExecuteSqlExpression](
      DOperationCategories.Transformation,
      "Executes an SQL expression on a DataFrame"
    )

    catalog.registerDOperation[FilterColumns](
      DOperationCategories.Transformation,
      "Creates a DataFrame containing only selected columns"
    )

    catalog.registerDOperation[HandleMissingValues](
      DOperationCategories.Transformation,
      "Handles missing values in a DataFrame"
    )

    catalog.registerDOperation[Join](
      DOperationCategories.DataManipulation,
      "Joins two DataFrames to a DataFrame"
    )

    catalog.registerDOperation[Split](
      DOperationCategories.DataManipulation,
      "Splits a DataFrame into two DataFrames"
    )

    catalog.registerDOperation[Union](
      DOperationCategories.DataManipulation,
      "Creates a DataFrame containing all rows from both input DataFrames"
    )
  }
}
