/**
 * Copyright 2015 deepsense.ai (CodiLime, Inc)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ai.deepsense.deeplang.catalogs.doperable

import scala.collection.mutable
import scala.reflect.runtime.{universe => ru}

import ai.deepsense.deeplang.{DOperable, TypeUtils}

/**
 * Allows to register and validate hierarchy of DClasses, DTraits and DOperations.
 * Exposes tools for advance reflecting and instances creation.
 */
class DOperableCatalog {
  private val baseType = ru.typeOf[DOperable]

  /** All registered nodes. Keys are type nodes fullNames. */
  private val nodes: mutable.Map[String, TypeNode] = mutable.Map()

  this.register(baseType)

  private def addNode(node: TypeNode): Unit = nodes(node.fullName) = node

  /**
   * Tries to register type in hierarchy.
   * Value t and javaType should be describing the same type.
   * @param t runtime type of class being registered
   * @param javaType represents class being registered
   * @return Some(node) if succeed and None otherwise
   */
  private def register(t: ru.Type, javaType: Class[_]): Option[TypeNode] = {
    if (!(t <:< baseType)) {
      return None
    }

    val node = TypeNode(javaType)

    val registeredNode = nodes.get(node.fullName)
    if (registeredNode.isDefined) {
      return registeredNode
    }

    val superTraits = javaType.getInterfaces.filter(_ != null).map(register).flatten
    superTraits.foreach(_.addSuccessor(node))
    superTraits.foreach(node.addSupertrait)

    val parentJavaType = node.getParentJavaType(baseType)
    if (parentJavaType.isDefined) {
      val parentClass = register(parentJavaType.get)
        if (parentClass.isDefined) {
          parentClass.get.addSuccessor(node)
          node.setParent(parentClass.get)
        }
    }

    addNode(node)
    Some(node)
  }

  /**
   * Tries to register type in hierarchy.
   * @param javaType represents class being registered
   * @return Some(node) if succeed and None otherwise
   */
  private def register(javaType: Class[_]): Option[TypeNode] = {
    register(TypeUtils.classToType(javaType), javaType)
  }

  /**
   * Tries to register type in hierarchy.
   * @param t runtime type of class being registered
   * @return Some(node) if succeed and None otherwise
   */
  def register(t: ru.Type): Option[TypeNode] = {
    register(t, TypeUtils.typeToClass(t, TypeUtils.classMirror(getClass)))
  }

  /**
    * Tries to register type in hierarchy.
    * @param t runtime `TypeTag` of class being registered
    * @return Some(node) if succeed and None otherwise
    */
  def register(t: ru.TypeTag[_]): Option[TypeNode] = {
    register(t.tpe, TypeUtils.typeTagToClass(t))
  }

  /**
   * Returns nodes that correspond given type signature.
   * For example, for type "A with T1 with T2", it returns three nodes corresponding
   * to A, T1 and T2. All classes and traits in given type that are not registered
   * in catalog will be ignored.
   * @tparam T type for which nodes are desired
   * @return sequence of all nodes corresponding to given type
   */
  private def nodesForType[T <: DOperable : ru.TypeTag]: Traversable[TypeNode] = {
    val allBases: List[ru.Symbol] = ru.typeOf[T].baseClasses

    // List 'allBases' contains symbols of all (direct and indirect) supertypes of T,
    // including T itself. If T is not complete type, but type signature
    // (e.g. "T with T1 with T2"), this list contains <refinement> object in the first place,
    // which we need to discard somehow.
    // TODO: find some better way to do it
    val baseClasses = allBases.filter(!_.fullName.endsWith("<refinement>"))

    // Now we discard all redundant types from list.
    var uniqueBaseClasses = Set[ru.Symbol]()
    for (b <- baseClasses) {
      val t: ru.Type = TypeUtils.symbolToType(b)
      val uniqueTypes = uniqueBaseClasses.map(TypeUtils.symbolToType)
      if (!uniqueTypes.exists(_ <:< t)) {
        uniqueBaseClasses += b
      }
    }

    val baseClassesNames: Set[String] = uniqueBaseClasses.map(_.fullName)
    nodes.filterKeys(baseClassesNames.contains).values
  }

  /**
   * Instances of all concrete classes that fulfil type signature T.
   * Type signature can have complex form, for example "A with T1 with T2".
   */
  def concreteSubclassesInstances[T <: DOperable : ru.TypeTag]: Set[T] = {
    val typeNodes = nodesForType[T]
    val concreteClassNodes = typeNodes.map(_.subclassesInstances)
    val intersect = DOperableCatalog.intersectSets[ConcreteClassNode](concreteClassNodes)
    intersect.map(_.createInstance[T])
  }

  /**
   * Registers type - either trait or class - in catalog. All of this type's superclasses
   * and supertraits that are subtypes of DOperable and have not been registered so far
   * will get registered now. Within registered hierarchy, traits cannot inherit from classes.
   * All registered classes that are not abstract have to expose parameterless constructor
   * (either primary or auxiliary). Registered types cannot be parametrized.
   */
  def registerDOperable[C <: DOperable : ru.TypeTag](): Unit = {
    this.register(ru.typeOf[C])
  }

  /** Returns descriptor that describes currently registered hierarchy. */
  def descriptor: HierarchyDescriptor = {
    val (traits, classes) = nodes.values.partition(_.isTrait)
    HierarchyDescriptor(
      traits.map(t => {
        val traitDescriptor = t.descriptor.asInstanceOf[TraitDescriptor]
        traitDescriptor.name -> traitDescriptor
      }).toMap,
      classes.map(c => {
        val classDescriptor = c.descriptor.asInstanceOf[ClassDescriptor]
        classDescriptor.name -> classDescriptor
      }).toMap)
  }
}

object DOperableCatalog {
  def apply() = new DOperableCatalog()
  /** Intersection of collection of sets. */
  private def intersectSets[T](sets: Traversable[Set[T]]): Set[T] = {
    if (sets.size == 0) {
      Set[T]()
    } else {
      sets.foldLeft(sets.head)((x, y) => x & y)
    }
  }
}
