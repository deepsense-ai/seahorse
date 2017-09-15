/*
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Witold Jedrzejewski
 */

package io.deepsense.deeplang

import scala.collection.mutable
import scala.reflect.runtime.{universe => ru}

import io.deepsense.deeplang.DHierarchy.{TraitInfo, ClassInfo, TypeInfo}

object DHierarchy {
  trait TypeInfo

  case class ClassInfo(
      val name: String,
      val parent: Option[String],
      val traits: List[String])
    extends TypeInfo

  case class TraitInfo(
      val name: String,
      val parents: List[String])
    extends TypeInfo
}

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

  private abstract class Node {
    protected val typeInfo: Class[_]
    private[DHierarchy] val isTrait: Boolean = typeInfo.isInterface

    protected var parent: Option[Node] = None
    protected val supertraits: mutable.Map[String, Node] = mutable.Map()
    protected val subclasses: mutable.Map[String, Node] = mutable.Map()
    protected val subtraits: mutable.Map[String, Node] = mutable.Map()
    /** Name that unambiguously defines underlying type. */
    private[DHierarchy] val fullName: String = typeInfo.getName.replaceAllLiterally("$", ".")
    /** The part of the full name after the last '.' */
    private[DHierarchy] val displayName: String = fullName.substring(fullName.lastIndexOf('.') + 1)

    private[DHierarchy] def addParent(node: Node): Unit = parent = Some(node)

    private[DHierarchy] def addSupertrait(node: Node): Unit = supertraits(node.fullName) = node

    private[DHierarchy] def addSuccessor(node: Node): Unit = {
      if (node.isTrait) addSubtrait(node) else addSubclass(node)
    }

    private def addSubclass(node: Node): Unit = subclasses(node.fullName) = node

    private def addSubtrait(node: Node): Unit = subtraits(node.fullName) = node

    private[DHierarchy] def info: TypeInfo

    private def sumSets[T](sets: Iterable[mutable.Set[T]]): mutable.Set[T] = {
      sets.foldLeft(mutable.Set[T]())((x, y) => x ++ y)
    }

    /** Returns set of all leaf-nodes that are descendants of this. */
    private[DHierarchy] def leafNodes: mutable.Set[Node] = {
      if (subclasses.isEmpty && subtraits.isEmpty) mutable.Set(this) // this is leaf-class
      else {
        val descendants = subclasses.values.map(_.leafNodes) ++ subtraits.values.map(_.leafNodes)
        sumSets[Node](descendants)
      }
    }

    /**
     * Creates instance of type represented by this.
     * Invokes first constructor and assumes that it takes no parameters.
     */
    private[DHierarchy] def createInstance(): DOperable = {
      val constructor = typeInfo.getConstructors()(0)
      constructor.newInstance().asInstanceOf[DOperable]
    }

    override def toString = s"Node($fullName)"
  }

  private class TraitNode(protected override val typeInfo: Class[_]) extends Node {

    private[DHierarchy] override def addParent(node: Node): Unit = {
      throw new RuntimeException // TODO: What is the exceptions convention here?
    }

    private[DHierarchy] override def info: TypeInfo = {
      TraitInfo(displayName, supertraits.values.map(_.displayName).toList)
    }
  }

  private class ClassNode(protected override val typeInfo: Class[_]) extends Node {

    private[DHierarchy] override def info: TypeInfo = {
      val parentName = if (parent.isDefined) Some(parent.get.displayName) else None
      ClassInfo(displayName, parentName, supertraits.values.map(_.displayName).toList)
    }
  }

  private object Node {
    def apply(typeInfo: Class[_]): Node = {
      if (typeInfo.isInterface) {
        new TraitNode(typeInfo)
      } else {
        new ClassNode(typeInfo)
      }
    }
  }

  private def classToType(c: Class[_]): ru.Type = mirror.classSymbol(c).toType

  private def typeToClass(t: ru.Type): Class[_] = mirror.runtimeClass(t.typeSymbol.asClass)

  private def symbolToType(s: ru.Symbol): ru.Type = s.asClass.toType

  private def addNode(node: Node): Unit = nodes(node.fullName) = node

  /**
   * Tries to register type in hierarchy.
   * Returns Some(node) if succeed and None otherwise.
   * Value t and typeInfo should be describing the same type.
   */
  private def register(t: ru.Type, typeInfo: Class[_]): Option[Node] = {
    if (!(t <:< baseType))
      return None

    val node = Node(typeInfo)

    val registeredNode = nodes.get(node.fullName)
    if (registeredNode.isDefined)
      return registeredNode

    val superTraits = typeInfo.getInterfaces.map(register).flatten
    superTraits.foreach(_.addSuccessor(node))
    superTraits.foreach(node.addSupertrait)

    val parentClass = register(typeInfo.getSuperclass)
    if (parentClass.isDefined) {
      parentClass.get.addSuccessor(node)
      node.addParent(parentClass.get)
    }

    addNode(node)
    Some(node)
  }

  private def register(typeInfo: Class[_]): Option[Node] = {
    if (typeInfo == null) return None
    register(classToType(typeInfo), typeInfo)
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

  def info: (Iterable[TypeInfo], Iterable[TypeInfo]) = {
    val (traits, classes) = nodes.values.partition(_.isTrait)
    (traits.map(_.info), classes.map(_.info))
  }
}
