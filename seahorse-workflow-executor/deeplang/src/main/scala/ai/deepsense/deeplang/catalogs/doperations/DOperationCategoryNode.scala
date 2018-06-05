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

package ai.deepsense.deeplang.catalogs.doperations

import scala.collection.immutable.SortedMap

import ai.deepsense.deeplang.DOperation
import ai.deepsense.deeplang.catalogs.SortPriority
import ai.deepsense.deeplang.catalogs.doperations.DOperationCategoryNode.InnerNodes

/**
 * Node in DOperationCategoryTree.
 * Represents certain category, holds its subcategories and assigned operations.
 * Objects of this class are immutable.
 * @param category category represented by this node or None if it is root
 * @param successors map from all direct child-categories to nodes representing them
 * @param operations operations directly in category represented by this node
 */
case class DOperationCategoryNode(
    category: Option[DOperationCategory] = None,
    successors: InnerNodes = DOperationCategoryNode.emptyInnerNodes,
    operations: List[DOperationDescriptor] = List.empty) {

  /**
   * Adds operation to node under given path of categories.
   * @param operation descriptor of operation to be added
   * @param path requested path of categories from this node to added operation
   * @return node identical to this but with operation added
   */
  private def addOperationAtPath(
      operation: DOperationDescriptor,
      path: List[DOperationCategory]): DOperationCategoryNode = {
    path match {
      case Nil => copy(operations = (operations :+ operation).sortWith(_.priority < _.priority))
      case category :: tail =>
        val successor = successors.getOrElse(category, DOperationCategoryNode(Some(category)))
        val updatedSuccessor = successor.addOperationAtPath(operation, tail)
        copy(successors = successors + (category -> updatedSuccessor))
    }
  }

  /**
   * Adds a new DOperation to the tree represented by this node under a specified category.
   * @param operation operation descriptor to be added
   * @param category category under which operation should directly be
   * @return category tree identical to this but with operation added
   */
  def addOperation(
      operation: DOperationDescriptor,
      category: DOperationCategory) : DOperationCategoryNode = {
    addOperationAtPath(operation, category.pathFromRoot)
  }

  /**
    * Gets all operations inside a tree
    * @return Sequence of tuples containing operation with its priority and category
    */
  def getOperations: Seq[(DOperationCategory, DOperation.Id, SortPriority)] = {
    val thisOperations = operations.map(operation => (operation.category, operation.id, operation.priority))
    val successorOperations = successors.collect { case successor => successor._2.getOperations }.flatten
    thisOperations ++ successorOperations
  }

  /**
    * Gets all categories inside a tree
    * @return Sequence of registrated categories
    */
  def getCategories: Seq[DOperationCategory] = {
    val successorOperations = successors.collect { case successor => successor._2.getCategories }.flatten
    successorOperations.toList ++ category
  }
}

object DOperationCategoryNode {
  type InnerNodes = SortedMap[(DOperationCategory), DOperationCategoryNode]
  def emptyInnerNodes = SortedMap[(DOperationCategory), DOperationCategoryNode]()
}
