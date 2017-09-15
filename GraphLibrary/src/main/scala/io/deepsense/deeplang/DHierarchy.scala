/*
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Witold Jedrzejewski
 */

package io.deepsense.deeplang

import scala.collection.mutable
import scala.reflect.runtime.{universe => ru}

/**
 * Allows to register and validate hierarchy of DClasses, DTraits and DOperations.
 * Exposes tools for advance reflecting and instances creation.
 */
class DHierarchy {
  private val mirror = ru.runtimeMirror(getClass.getClassLoader)
  private val baseType = ru.typeOf[DOperable]
  /** All registered nodes. */
  private val nodes: mutable.Map[String, Node] = mutable.Map()

  this.register(baseType)

  private class Node(private val classInfo: Class[_]) {
    /** Set of all direct subclasses and subtraits. */
    private val successors: mutable.Map[String, Node] = mutable.Map()
    /** Set of all direct superclasses and supertraits. */
    private val predecessors: mutable.Map[String, Node] = mutable.Map()
    /** Name that unambiguously defines underlying type. */
    private[DHierarchy] val name: String = classInfo.getName.replaceAllLiterally("$", ".")
    private[DHierarchy] val isTrait: Boolean = classInfo.isInterface

    private[DHierarchy] def addSuccessor(node: Node): Unit = {
      successors(node.name) = node
    }

    private[DHierarchy] def addPredecessor(node: Node): Unit = {
      predecessors(node.name) = node
    }

    private def sumSets[T](sets: Iterable[mutable.Set[T]]) : mutable.Set[T] = {
      sets.foldLeft(mutable.Set[T]())((x,y) => x++y)
    }

    /** Returns set of all leaf-nodes that are descendants of this. */
    private[DHierarchy] def leafNodes: mutable.Set[Node] = {
      if (successors.isEmpty) mutable.Set(this) // this is leaf-class
      else {
        val descendants = successors.values.map(_.leafNodes)
        sumSets[Node](descendants)
      }
    }

    /**
     * Creates instance of type represented by this.
     * Invokes first constructor and assumes that it takes no parameters.
     */
    private[DHierarchy] def createInstance(): DOperable = {
      val constructor = classInfo.getConstructors()(0)
      constructor.newInstance().asInstanceOf[DOperable]
    }

    override def toString = s"Node($name)"
  }

  private def classToType(c: Class[_]): ru.Type = mirror.classSymbol(c).toType

  private def typeToClass(t: ru.Type): Class[_] = mirror.runtimeClass(t.typeSymbol.asClass)

  private def symbolToType(s: ru.Symbol): ru.Type = s.asClass.toType

  private def addNode(node: Node): Unit = nodes(node.name) = node

  /**
   * Tries to register type in hierarchy.
   * Returns Some(node) if succeed and None otherwise.
   * Value t and classInfo should be describing the same type.
   */
  private def register(t: ru.Type, classInfo: Class[_]): Option[Node] = {
    if (!(t <:< baseType))
      return None

    val node = new Node(classInfo)

    val registeredNode = nodes.get(node.name)
    if (registeredNode.isDefined)
      return registeredNode

    val superTypes = classInfo.getInterfaces :+ classInfo.getSuperclass
    val superNodes = superTypes.map(register).flatten
    superNodes.foreach(_.addSuccessor(node))
    superNodes.foreach(node.addPredecessor(_))
    addNode(node)
    Some(node)
  }

  private def register(classInfo: Class[_]): Option[Node] = {
    if (classInfo == null) return None
    register(classToType(classInfo), classInfo)
  }

  private def register(t: ru.Type): Option[Node] = {
    register(t, typeToClass(t))
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
      val t: ru.Type = symbolToType(b)
      val uniqueTypes = uniqueBaseClasses.map(symbolToType)
      if (!uniqueTypes.exists(_ <:< t))
        uniqueBaseClasses += b
    }

    val baseClassesNames: Set[String] = uniqueBaseClasses.map(_.fullName)
    nodes.filterKeys(baseClassesNames.contains).values
  }

  /** Intersection of collection of sets. */
  private def intersectSets[T](sets: Traversable[mutable.Set[T]]): mutable.Set[T] = {
    if (sets.size == 0) mutable.Set[T]()
    else sets.foldLeft(sets.head)((x, y) => x & y)
  }

  /** Returns instances of all leaf-classes that fulfil type signature T. */
  def concreteSubclassesInstances[T: ru.TypeTag]: mutable.Set[DOperable] = {
    val typeNodes = nodesForType[T]
    val leafNodes = typeNodes.map(_.leafNodes)
    val intersect = intersectSets[Node](leafNodes)
    intersect.map(_.createInstance())
  }

  def registerDOperable[C <: DOperable : ru.TypeTag](): Unit = {
    this.register(ru.typeOf[C])
  }

  def registerDOperation[T <: DOperation](): Unit = ???
}
