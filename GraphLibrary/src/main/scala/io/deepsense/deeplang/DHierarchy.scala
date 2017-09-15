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
  /** All registered classNodes. */
  private val classNodes: mutable.Map[String, ClassNode] = mutable.Map()

  this.register(baseType)

  private class ClassNode(private val classInfo: Class[_]) {
    /** Set of all direct subclasses and subtraits. */
    private val successors: mutable.Map[String, ClassNode] = mutable.Map()
    /** Name that unambiguously defines underlying type. */
    private[DHierarchy] val name: String = classInfo.getName.replaceAllLiterally("$", ".")

    private[DHierarchy] def addSubclass(classNode: ClassNode): Unit = {
      successors(classNode.name) = classNode
    }

    private def sumSets[T](sets: Iterable[mutable.Set[T]]) : mutable.Set[T] = {
      sets.foldLeft(mutable.Set[T]())((x,y) => x++y)
    }

    /** Returns set of all leaf-classes that are descendants of this. */
    private[DHierarchy] def leafClassNodes: mutable.Set[ClassNode] = {
      if (successors.isEmpty) mutable.Set(this) // this is leaf-class
      else {
        val descendants = successors.values.map(_.leafClassNodes)
        sumSets[ClassNode](descendants)
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

    override def toString = s"ClassNode($name)"
  }

  private def classToType(c: Class[_]): ru.Type = mirror.classSymbol(c).toType

  private def typeToClass(t: ru.Type): Class[_] = mirror.runtimeClass(t.typeSymbol.asClass)

  private def symbolToType(s: ru.Symbol): ru.Type = s.asClass.toType

  private def addClassNode(classNode: ClassNode): Unit = classNodes(classNode.name) = classNode

  /**
   * Tries to register type in hierarchy.
   * Returns Some(classNode) if succeed and None otherwise.
   * Value t and classInfo should be describing the same type.
   */
  private def register(t: ru.Type, classInfo: Class[_]): Option[ClassNode] = {
    if (!(t <:< baseType))
      return None

    val classNode = new ClassNode(classInfo)

    val registeredClassNode = classNodes.get(classNode.name)
    if (registeredClassNode.isDefined)
      return registeredClassNode

    val superTypes = classInfo.getInterfaces :+ classInfo.getSuperclass
    val superNodes = superTypes.map(register).flatten
    superNodes.foreach(_.addSubclass(classNode))
    addClassNode(classNode)
    Some(classNode)
  }

  private def register(classInfo: Class[_]): Option[ClassNode] = {
    if (classInfo == null) return None
    register(classToType(classInfo), classInfo)
  }

  private def register(t: ru.Type): Option[ClassNode] = {
    register(t, typeToClass(t))
  }

  /** Returns classNodes that correspond given type signature T. */
  private def classNodesForType[T: ru.TypeTag]: Traversable[ClassNode] = {
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
    classNodes.filterKeys(baseClassesNames.contains).values
  }

  /** Intersection of collection of sets. */
  private def intersectSets[T](sets: Traversable[mutable.Set[T]]): mutable.Set[T] = {
    if (sets.size == 0) mutable.Set[T]()
    else sets.foldLeft(sets.head)((x, y) => x & y)
  }

  /** Returns instances of all leaf-classes that fulfil type signature T. */
  def concreteSubclassesInstances[T: ru.TypeTag]: mutable.Set[DOperable] = {
    val typeClassNodes = classNodesForType[T]
    val leafClassNodes = typeClassNodes.map(_.leafClassNodes)
    val intersect = intersectSets[ClassNode](leafClassNodes)
    intersect.map(_.createInstance())
  }

  def registerDOperable[C <: DOperable : ru.TypeTag](): Unit = {
    this.register(ru.typeOf[C])
  }

  def registerDOperation[T <: DOperation](): Unit = ???
}
