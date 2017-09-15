/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Witold Jedrzejewski
 */

package io.deepsense.deeplang.catalogs.doperable

import scala.collection.mutable
import scala.reflect.runtime.{universe => ru}

/**
 * Node that represents type in DHierarchy graph.
 */
private[doperable] abstract class Node {
  protected val javaType: Class[_]
  private[doperable] val isTrait: Boolean = javaType.isInterface

  protected var parent: Option[Node] = None
  protected val supertraits: mutable.Map[String, Node] = mutable.Map()
  protected val subclasses: mutable.Map[String, Node] = mutable.Map()
  protected val subtraits: mutable.Map[String, Node] = mutable.Map()

  /** Name that unambiguously defines underlying type. */
  private[doperable] val fullName: String = javaType.getName.replaceAllLiterally("$", ".")
  /** The part of the full name after the last '.' */
  private[doperable] val displayName: String = fullName.substring(fullName.lastIndexOf('.') + 1)

  private[doperable] def addParent(node: Node): Unit = parent = Some(node)

  private[doperable] def addSupertrait(node: Node): Unit = supertraits(node.fullName) = node

  private[doperable] def addSuccessor(node: Node): Unit = {
    if (node.isTrait) addSubtrait(node) else addSubclass(node)
  }

  private def addSubclass(node: Node): Unit = subclasses(node.fullName) = node

  private def addSubtrait(node: Node): Unit = subtraits(node.fullName) = node

  /**
   * Returns java type of parent DClass of node if such parent exists.
   */
  private[doperable] def getParentJavaType(upperBoundType: ru.Type): Option[Class[_]]

  private[doperable] def descriptor: TypeDescriptor

  private def sumSets[T](sets: Iterable[Set[T]]): Set[T] = {
    sets.foldLeft(Set[T]())((x, y) => x ++ y)
  }

  /** Returns set of all concrete nodes that are descendants of this. */
  private[doperable] def subclassesInstances: Set[ConcreteClassNode] = {
    val descendants = subclasses.values.map(_.subclassesInstances) ++
        subtraits.values.map(_.subclassesInstances)
    sumSets[ConcreteClassNode](descendants)
  }
}

private[doperable] object Node {
  def apply(javaType: Class[_]): Node = {
    if (javaType.isInterface) TraitNode(javaType) else ClassNode(javaType)
  }
}

