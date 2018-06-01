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
import ai.deepsense.deeplang.catalogs.doperations.{DCategoryCatalog, DOperationCategory, DOperationsCatalog}
import ai.deepsense.deeplang.catalogs.spi.CatalogRegistrar.{OperableType, OperationFactory}
import ai.deepsense.deeplang.catalogs.{DCatalog, SortPriority}
import ai.deepsense.deeplang.{DOperable, DOperation}

import scala.reflect.runtime.universe._

/**
  * Service context interface for enabling CatalogRegistrant-s add their categories, operations, and operables
  * to the system.
  */
trait CatalogRegistrar {
  def registerCategory(category: DOperationCategory, priority: SortPriority): Unit
  def registeredCategories: Seq[(DOperationCategory, SortPriority)]

  def registerOperation(category: DOperationCategory, factory: OperationFactory, priority: SortPriority): Unit
  def registeredOperations: Seq[(DOperationCategory, OperationFactory, SortPriority)]

  def registerOperable[C <: DOperable : TypeTag](priority: SortPriority = SortPriority.DEFAULT): Unit
  def registeredOperables: Seq[(OperableType, SortPriority)]
}

object CatalogRegistrar {
  type OperationFactory = () => DOperation
  type OperableType = TypeTag[_ <: DOperable]

  class DefaultCatalogRegistrar() extends CatalogRegistrar {
    private var categories = Seq.empty[(DOperationCategory, SortPriority)]
    private var operations = Seq.empty[(DOperationCategory, OperationFactory, SortPriority)]
    private var operables = Seq.empty[(OperableType, SortPriority)]

    override def registerCategory(category: DOperationCategory, priority: SortPriority): Unit =
      categories = categories :+ ((category, priority))

    override def registeredCategories: Seq[(DOperationCategory, SortPriority)] = categories

    override def registerOperation(
      category: DOperationCategory, factory: OperationFactory, priority: SortPriority): Unit =
      operations = operations :+ ((category, factory, priority))

    override def registeredOperations: Seq[(DOperationCategory, OperationFactory, SortPriority)] = operations

    override def registerOperable[C <: DOperable : TypeTag](priority: SortPriority): Unit =
      operables = operables :+ ((typeTag[C], priority))

    override def registeredOperables: Seq[(OperableType, SortPriority)] = operables

    /** Constructs a DCatalog from the set of registered deeplang components. */
    def catalog: DCatalog = {
      val cats = DCategoryCatalog()
      categories.sortBy(_._2).foreach {
        case (cat, _) => cats.registerDCategory(cat)
      }

      val ops = DOperationsCatalog()
      operations.sortBy(_._3).foreach {
        case (cat, fact, _) => ops.registerDOperation(cat, fact)
      }

      val opr = DOperableCatalog()
      operables.sortBy(_._2).foreach {
        case (tpe, _) => opr.register(tpe)
      }
      new DCatalog(cats, opr, ops)
    }
  }
}
