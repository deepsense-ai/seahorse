/**
 * Copyright 2018 Astraea, Inc.
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

package ai.deepsense.deeplang.catalogs.spi

import ai.deepsense.deeplang.catalogs.doperable.DOperableCatalog
import ai.deepsense.deeplang.catalogs.doperations.{DOperationCategory, DOperationsCatalog}
import ai.deepsense.deeplang.catalogs.spi.CatalogRegistrar.{OperableType, OperationFactory}
import ai.deepsense.deeplang.catalogs.{DCatalog, SortPriority}
import ai.deepsense.deeplang.{DOperable, DOperation}

import scala.reflect.runtime.universe._

/**
  * Service context interface for enabling CatalogRegistrant-s add their categories, operations, and operables
  * to the system.
  */
trait CatalogRegistrar {
  def registeredCategories: Seq[DOperationCategory]
  def registeredOperations: Seq[(DOperationCategory, OperationFactory, SortPriority)]

  def registerOperation(category: DOperationCategory, factory: OperationFactory, priority: SortPriority): Unit
  def registerOperable[C <: DOperable : TypeTag](): Unit
}

object CatalogRegistrar {
  type OperationFactory = () => DOperation
  type OperableType = TypeTag[_ <: DOperable]

  class DefaultCatalogRegistrar() extends CatalogRegistrar {
    private val operationsCatalog = DOperationsCatalog()
    private var operables = DOperableCatalog()

    override def registeredCategories: Seq[DOperationCategory] = operationsCatalog.categoryTree.getCategories
    override def registeredOperations: Seq[(DOperationCategory, OperationFactory, SortPriority)] =
      operationsCatalog.registeredOperations

    override def registerOperation(
      category: DOperationCategory, factory: OperationFactory, priority: SortPriority): Unit =
      operationsCatalog.registerDOperation(category, factory, priority)

    override def registerOperable[C <: DOperable : TypeTag](): Unit =
      operables.register(typeTag[C])

    /** Constructs a DCatalog from the set of registered deeplang components. */
    def catalog: DCatalog = {
      new DCatalog(operationsCatalog.categories, operables, operationsCatalog)
    }
  }
}
