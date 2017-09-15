/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Witold Jedrzejewski
 */

package io.deepsense.deeplang.dhierarchy

import scala.collection.mutable
import scala.reflect.runtime.{universe => ru}

import io.deepsense.deeplang.DOperable
import io.deepsense.deeplang.dhierarchy.exceptions.ParametrizedTypeException

/**
 * Allows to register and validate hierarchy of DClasses, DTraits and DOperations.
 * Exposes tools for advance reflecting and instances creation.
 */
class DHierarchy {
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

    if (DHierarchy.isParametrized(t)) {
      throw new ParametrizedTypeException(t)
    }

    val node = Node(javaType)

    val registeredNode = nodes.get(node.fullName)
    if (registeredNode.isDefined)
      return registeredNode

    val superTraits = javaType.getInterfaces.map(register).flatten
    superTraits.foreach(_.addSuccessor(node))
    superTraits.foreach(node.addSupertrait)

    val parentClass = register(javaType.getSuperclass)
    if (parentClass.isDefined) {
      parentClass.get.addSuccessor(node)
      node.addParent(parentClass.get)
    }

    addNode(node)
    Some(node)
  }

  private def register(javaType: Class[_]): Option[Node] = {
    if (javaType == null) return None
    register(DHierarchy.classToType(javaType), javaType)
  }

  private def register(t: ru.Type): Option[Node] = {
    register(t, DHierarchy.typeToClass(t))
  }

  /** Returns nodes that correspond given type signature T. */
  private def nodesForType[T: ru.TypeTag]: Traversable[Node] = {
    val allBases: List[ru.Symbol] = ru.typeOf[T].baseClasses

    // 'allBases' contains symbols of all (direct and indirect) superclasses of T,
    // including T itself. If T is not complete type, but type signature
    // (e.g. "T with T1 with T2"), this list contains <refinement> object in the first place,
    // which we need to discard.
    // TODO: find some better way to do it
    val baseClasses = allBases.filter(!_.fullName.endsWith("<refinement>"))

    var uniqueBaseClasses = Set[ru.Symbol]()
    for (b <- baseClasses) {
      val t: ru.Type = DHierarchy.symbolToType(b)
      val uniqueTypes = uniqueBaseClasses.map(DHierarchy.symbolToType)
      if (!uniqueTypes.exists(_ <:< t))
        uniqueBaseClasses += b
    }

    val baseClassesNames: Set[String] = uniqueBaseClasses.map(_.fullName)
    nodes.filterKeys(baseClassesNames.contains).values
  }

  /** Returns instances of all concrete classes that fulfil type signature T. */
  def concreteSubclassesInstances[T: ru.TypeTag]: mutable.Set[DOperable] = {
    val typeNodes = nodesForType[T]
    val concreteClassNodes = typeNodes.map(_.subclassesInstances)
    val intersect = DHierarchy.intersectSets[ConcreteClassNode](concreteClassNodes)
    intersect.map(_.createInstance())
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

object DHierarchy {
  private val mirror = ru.runtimeMirror(getClass.getClassLoader)

  private def classToType(c: Class[_]): ru.Type = mirror.classSymbol(c).toType

  private def typeToClass(t: ru.Type): Class[_] = mirror.runtimeClass(t.typeSymbol.asClass)

  private def symbolToType(s: ru.Symbol): ru.Type = s.asClass.toType

  private def isParametrized(t: ru.Type): Boolean = !t.typeSymbol.asClass.typeParams.isEmpty

  /** Intersection of collection of sets. */
  private def intersectSets[T](sets: Traversable[mutable.Set[T]]): mutable.Set[T] = {
    if (sets.size == 0) mutable.Set[T]()
    else sets.foldLeft(sets.head)((x, y) => x & y)
  }

  private[dhierarchy] def isAbstract(c: Class[_]): Boolean = {
    classToType(c).typeSymbol.asClass.isAbstractClass
  }
}
