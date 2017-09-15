/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.catalogs.doperations

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
    successors: Map[DOperationCategory, DOperationCategoryNode] = Map.empty,
    operations: Set[DOperationDescriptor] = Set.empty) {

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
      case Nil => copy(operations = operations + operation)
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
}
