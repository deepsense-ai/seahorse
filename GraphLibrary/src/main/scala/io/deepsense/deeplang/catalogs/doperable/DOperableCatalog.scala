/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Witold Jedrzejewski
 */

package io.deepsense.deeplang.catalogs.doperable

import scala.collection.mutable
import scala.reflect.runtime.{universe => ru}
import io.deepsense.deeplang.catalogs.doperable.exceptions.ParametrizedTypeException
import io.deepsense.deeplang.{DOperable, TypeUtils}

/**
 * Allows to register and validate hierarchy of DClasses, DTraits and DOperations.
 * Exposes tools for advance reflecting and instances creation.
 */
class DOperableCatalog {
  private val baseType = ru.typeOf[DOperable]
  /** All registered nodes. */
  private val nodes: mutable.Map[String, Node] = mutable.Map()

  this.register(baseType)

  private def addNode(node: Node): Unit = nodes(node.fullName) = node

  /**
   * Tries to register type in hierarchy.
   * Returns Some(node) if succeed and None otherwise.
   * Value t and javaType should be describing the same type.
   */
  private def register(t: ru.Type, javaType: Class[_]): Option[Node] = {
    if (!(t <:< baseType)) {
      return None
    }

    if (TypeUtils.isParametrized(t)) {
      throw new ParametrizedTypeException(t)
    }

    val node = Node(javaType)

    val registeredNode = nodes.get(node.fullName)
    if (registeredNode.isDefined)
      return registeredNode

    val superTraits = javaType.getInterfaces.filter(_ != null).map(register).flatten
    superTraits.foreach(_.addSuccessor(node))
    superTraits.foreach(node.addSupertrait)

    val parentJavaType = node.getParentJavaType(baseType)
    if (parentJavaType.isDefined) {
      val parentClass = register(parentJavaType.get)
        if (parentClass.isDefined) {
          parentClass.get.addSuccessor(node)
          node.addParent(parentClass.get)
        }
    }

    addNode(node)
    Some(node)
  }

  private def register(javaType: Class[_]): Option[Node] = {
    register(TypeUtils.classToType(javaType), javaType)
  }

  private def register(t: ru.Type): Option[Node] = {
    register(t, TypeUtils.typeToClass(t))
  }

  /** Returns nodes that correspond given type signature T. */
  private def nodesForType[T <: DOperable : ru.TypeTag]: Traversable[Node] = {
    val allBases: List[ru.Symbol] = ru.typeOf[T].baseClasses

    // 'allBases' contains symbols of all (direct and indirect) superclasses of T,
    // including T itself. If T is not complete type, but type signature
    // (e.g. "T with T1 with T2"), this list contains <refinement> object in the first place,
    // which we need to discard.
    // TODO: find some better way to do it
    val baseClasses = allBases.filter(!_.fullName.endsWith("<refinement>"))

    var uniqueBaseClasses = Set[ru.Symbol]()
    for (b <- baseClasses) {
      val t: ru.Type = TypeUtils.symbolToType(b)
      val uniqueTypes = uniqueBaseClasses.map(TypeUtils.symbolToType)
      if (!uniqueTypes.exists(_ <:< t))
        uniqueBaseClasses += b
    }

    val baseClassesNames: Set[String] = uniqueBaseClasses.map(_.fullName)
    nodes.filterKeys(baseClassesNames.contains).values
  }

  /** Returns instances of all concrete classes that fulfil type signature T. */
  def concreteSubclassesInstances[T <: DOperable : ru.TypeTag]: Set[T] = {
    val typeNodes = nodesForType[T]
    val concreteClassNodes = typeNodes.map(_.subclassesInstances)
    val intersect = DOperableCatalog.intersectSets[ConcreteClassNode](concreteClassNodes)
    intersect.map(_.createInstance[T])
  }

  def registerDOperable[C <: DOperable : ru.TypeTag](): Unit = {
    this.register(ru.typeOf[C])
  }

  /** Returns structure describing hierarchy. */
  def info: (Iterable[TypeInfo], Iterable[TypeInfo]) = {
    val (traits, classes) = nodes.values.partition(_.isTrait)
    (traits.map(_.info), classes.map(_.info))
  }
}

object DOperableCatalog {
  /** Intersection of collection of sets. */
  private def intersectSets[T](sets: Traversable[Set[T]]): Set[T] = {
    if (sets.size == 0) Set[T]()
    else sets.foldLeft(sets.head)((x, y) => x & y)
  }
}
