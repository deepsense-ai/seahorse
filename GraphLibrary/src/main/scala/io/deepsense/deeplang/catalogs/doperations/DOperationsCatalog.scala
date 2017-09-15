/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Witold Jedrzejewski
 */

package io.deepsense.deeplang.catalogs.doperations

import java.lang.reflect.Constructor

import scala.collection.mutable
import scala.reflect.runtime.{universe => ru}
import io.deepsense.deeplang.catalogs.doperations.exceptions._
import io.deepsense.deeplang.{DOperation, TypeUtils}

case class DOperationsCatalog() {
  private val operationsTree = DOperationCategoryTree()
  private val operationsConstructors = mutable.Map.empty[String, Constructor[_]]
  private val operations = mutable.Set.empty[DOperationDescriptor]

  private def constructorForType(operationType: ru.Type) = {
    TypeUtils.constructorForType(operationType) match {
      case Some(x) => x
      case None => throw NoParameterLessConstructorInDOperationException(operationType)
    }
  }

  /**
   * Registers DOperation, which can be later viewed and created.
   * DOperation has to have parameter-less constructor.
   * @param name name for given operation
   * @param category category to which this operation directly belongs
   * @param description description of operation
   * @tparam T DOperation class to register
   */
  def registerDOperation[T <: DOperation : ru.TypeTag](
      name: String,
      category: DOperationCategory,
      description: String): Unit = {
    val operationType = ru.typeOf[T]
    val constructor = constructorForType(operationType)
    val operationInstance = DOperationsCatalog.createDOperation(constructor)
    val inPortTypes = operationInstance.inPortTypes.map(_.tpe)
    val outPortTypes = operationInstance.outPortTypes.map(_.tpe)
    val operationDescriptor = DOperationDescriptor(
        name, description, category, inPortTypes, outPortTypes)

    operations += operationDescriptor
    operationsTree.addOperation(operationDescriptor, category)
    operationsConstructors(name) = constructor
  }

  /**
   * Creates instance of requested DOperation class.
   * @param name name that was provided during desired DOperation registration
   */
  def createDOperation(name: String): DOperation = operationsConstructors.get(name) match {
    case Some(constructor) => DOperationsCatalog.createDOperation(constructor)
    case None => throw DOperationNotFoundException(name)
  }
}

object DOperationsCatalog {
  private[DOperationsCatalog] def createDOperation(constructor: Constructor[_]) = {
    TypeUtils.createInstance[DOperation](constructor)
  }
}
