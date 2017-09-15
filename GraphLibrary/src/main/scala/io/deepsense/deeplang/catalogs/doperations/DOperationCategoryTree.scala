/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Witold Jedrzejewski
 */

package io.deepsense.deeplang.catalogs.doperations

import scala.collection.mutable

/**
 * Tracks the registered DOperations in a hierarchical structure of categories.
 */
case class DOperationCategoryTree() {
  private case class DOperationCategoryTreeNode() {
    val successors: mutable.Map[DOperationCategory, DOperationCategoryTreeNode] = mutable.Map()
    /** Set of all operations that added directly to this category */
    val operations: mutable.Set[DOperationDescriptor] = mutable.Set()

    /**
     * Gets successor associated with given category.
     * If it does not exist, it is created and returned.
     */
    private def getOrCreateSuccessor(category: DOperationCategory): DOperationCategoryTreeNode = {
      successors.get(category) match {
        case Some(successor) => successor
        case None =>
          val successor = DOperationCategoryTreeNode()
          successors(category) = successor
          successor
      }
    }

    /**
     * Adds operation to node under given path of categories.
     * @param operation descriptor of operation to be added
     * @param path requested path of categories from this node to added operation
     */
    private[DOperationCategoryTree] def addOperationAtPath(
        operation: DOperationDescriptor,
        path: List[DOperationCategory]): Unit = {
      path match {
        case Nil => operations += operation
        case category :: tail =>
          val successor = getOrCreateSuccessor(category)
          successor.addOperationAtPath(operation, tail)
      }
    }
  }

  private val root = new DOperationCategoryTreeNode()

  /** Adds a new DOperation to the tree under a specified category. */
  def addOperation(operation: DOperationDescriptor, category: DOperationCategory) : Unit = {
    root.addOperationAtPath(operation, category.pathFromRoot)
  }
}
