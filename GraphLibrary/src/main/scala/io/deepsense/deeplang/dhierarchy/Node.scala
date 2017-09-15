/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Witold Jedrzejewski
 */

package io.deepsense.deeplang.dhierarchy

import scala.collection.mutable
import scala.reflect.runtime.{universe => ru}

/**
 * Node that represents type in DHierarchy graph.
 */
private[dhierarchy] abstract class Node {
  protected val javaType: Class[_]
  private[dhierarchy] val isTrait: Boolean = javaType.isInterface

  protected var parent: Option[Node] = None
  protected val supertraits: mutable.Map[String, Node] = mutable.Map()
  protected val subclasses: mutable.Map[String, Node] = mutable.Map()
  protected val subtraits: mutable.Map[String, Node] = mutable.Map()

  /** Name that unambiguously defines underlying type. */
  private[dhierarchy] val fullName: String = javaType.getName.replaceAllLiterally("$", ".")
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

  /** Returns set of all concrete nodes that are descendants of this. */
  private[dhierarchy] def subclassesInstances: mutable.Set[ConcreteClassNode] = {
    val descendants = subclasses.values.map(_.subclassesInstances) ++
        subtraits.values.map(_.subclassesInstances)
    sumSets[ConcreteClassNode](descendants)
  }
}

private[dhierarchy] object Node {
  def apply(javaType: Class[_]): Node = {
    if (javaType.isInterface) TraitNode(javaType) else ClassNode(javaType)
  }
}

