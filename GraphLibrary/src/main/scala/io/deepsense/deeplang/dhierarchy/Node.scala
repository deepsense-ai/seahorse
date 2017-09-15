/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Witold Jedrzejewski
 */

package io.deepsense.deeplang.dhierarchy

import scala.collection.mutable

import io.deepsense.deeplang.DOperable

/**
 * Node that represents type in DHierarchy graph.
 */
private[dhierarchy] abstract class Node {
  protected val typeInfo: Class[_]
  private[dhierarchy] val isTrait: Boolean = typeInfo.isInterface

  protected var parent: Option[Node] = None
  protected val supertraits: mutable.Map[String, Node] = mutable.Map()
  protected val subclasses: mutable.Map[String, Node] = mutable.Map()
  protected val subtraits: mutable.Map[String, Node] = mutable.Map()

  /** Name that unambiguously defines underlying type. */
  private[dhierarchy] val fullName: String = typeInfo.getName.replaceAllLiterally("$", ".")
  /** The part of the full name after the last '.' */
  private[dhierarchy] val displayName: String = fullName.substring(fullName.lastIndexOf('.') + 1)

  private[dhierarchy] def addParent(node: Node): Unit = parent = Some(node)

  private[dhierarchy] def addSupertrait(node: Node): Unit = supertraits(node.fullName) = node

  private[dhierarchy] def addSuccessor(node: Node): Unit = {
    if (node.isTrait) addSubtrait(node) else addSubclass(node)
  }

  private def addSubclass(node: Node): Unit = subclasses(node.fullName) = node

  private def addSubtrait(node: Node): Unit = subtraits(node.fullName) = node

  private[dhierarchy] def info: TypeInfo

  private def sumSets[T](sets: Iterable[mutable.Set[T]]): mutable.Set[T] = {
    sets.foldLeft(mutable.Set[T]())((x, y) => x ++ y)
  }

  /** Returns set of all leaf-nodes that are descendants of this. */
  private[dhierarchy] def leafNodes: mutable.Set[Node] = {
    if (subclasses.isEmpty && subtraits.isEmpty) { // this is leaf-class
      mutable.Set(this)
    } else {
      val descendants = subclasses.values.map(_.leafNodes) ++ subtraits.values.map(_.leafNodes)
      sumSets[Node](descendants)
    }
  }

  /**
   * Creates instance of type represented by this.
   * Invokes first constructor and assumes that it takes no parameters.
   */
  private[dhierarchy] def createInstance(): DOperable = {
    val constructor = typeInfo.getConstructors()(0)
    constructor.newInstance().asInstanceOf[DOperable]
  }
}

private[dhierarchy] object Node {
  def apply(typeInfo: Class[_]): Node = {
    if (typeInfo.isInterface) {
      new TraitNode(typeInfo)
    } else {
      new ClassNode(typeInfo)
    }
  }
}

