/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Michal Iwanowski
 */

package io.deepsense.deeplang.dhierarchy

/**
 * Category of DOperations.
 * It can be subcategory of different DOperationCategory.
 * @param name display name of this category
 * @param parent super-category of this one, if None this is top-level category
 */
abstract class DOperationCategory(val name: String, val parent: Option[DOperationCategory] = None) {
  def this(name: String, parent: DOperationCategory) = this(name, Some(parent))

  /** List of categories on path from this category to some top-level category. */
  private[dhierarchy] def pathToRoot: List[DOperationCategory] = parent match {
    case Some(category) => this :: category.pathToRoot
    case None => List()
  }

  /** List of categories on path from some top-level category to this category. */
  private[dhierarchy] def pathFromRoot: List[DOperationCategory] = pathToRoot.reverse
}
