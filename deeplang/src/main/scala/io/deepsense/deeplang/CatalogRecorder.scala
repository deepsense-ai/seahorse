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
  }

  def registerDOperations(catalog: DOperationsCatalog): Unit = {

    catalog.registerDOperation[Split](
      DOperationCategories.DataManipulation,
      "Splits a DataFrame into two DataFrames"
    )
  }
}
