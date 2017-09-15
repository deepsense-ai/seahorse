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
      DOperationCategories.IO)

    catalog.registerDOperation[WriteDataFrame](
      DOperationCategories.IO)

    catalog.registerDOperation[Notebook](
      DOperationCategories.IO)

    catalog.registerDOperation[CustomPythonOperation](
      DOperationCategories.Transformation)

    catalog.registerDOperation[ExecuteMathematicalTransformation](
      DOperationCategories.Transformation)

    catalog.registerDOperation[ConvertType](
      DOperationCategories.Transformation)

    catalog.registerDOperation[DecomposeDatetime](
      DOperationCategories.Transformation)

    catalog.registerDOperation[ExecuteMathematicalTransformation](
      DOperationCategories.Transformation)

    catalog.registerDOperation[ExecuteSqlExpression](
      DOperationCategories.Transformation)

    catalog.registerDOperation[FilterColumns](
      DOperationCategories.Transformation)

    catalog.registerDOperation[HandleMissingValues](
      DOperationCategories.Transformation)

    catalog.registerDOperation[Transform](
      DOperationCategories.Transformation)

    catalog.registerDOperation[Fit](
      DOperationCategories.DataManipulation)

    catalog.registerDOperation[Join](
      DOperationCategories.DataManipulation)

    catalog.registerDOperation[Split](
      DOperationCategories.DataManipulation)

    catalog.registerDOperation[Union](
      DOperationCategories.DataManipulation)
  }
}
