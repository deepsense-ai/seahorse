/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Witold Jedrzejewski
 */

package io.deepsense.deeplang.catalogs.doperations

import java.lang.reflect.Constructor
import java.util.UUID

import scala.collection.mutable
import scala.reflect.runtime.{universe => ru}

import io.deepsense.deeplang.catalogs.doperations.exceptions._
import io.deepsense.deeplang.{DOperation, TypeUtils}

/**
 * Catalog of DOperations.
 * Allows to register DOperations under specified categories,
 * to browse categories structure and to create operations instances.
 */
abstract class DOperationsCatalog {
  /** Tree describing categories structure. */
  def categoryTree: DOperationCategoryNode

  /** Map of all registered operation descriptors, where their ids are keys. */
  def operations: Map[UUID, DOperationDescriptor]

  /**
   * Creates instance of requested DOperation class.
   * @param name name that was provided during desired DOperation registration
   */
  def createDOperation(name: String): DOperation

  /**
   * Registers DOperation, which can be later viewed and created.
   * DOperation has to have parameterless constructor.
   * @param id id of operation
   * @param category category to which this operation directly belongs
   * @param description description of operation
   * @tparam T DOperation class to register
   */
  // TODO: currently operation.name identifies operation.
  // Move id field to DOperation and let id identify operation
  def registerDOperation[T <: DOperation : ru.TypeTag](
      id: UUID,
      category: DOperationCategory,
      description: String): Unit
}

object DOperationsCatalog {
  def apply(): DOperationsCatalog = new DOperationsCatalogImpl

  private[DOperationsCatalog] def createDOperation(constructor: Constructor[_]) = {
    TypeUtils.createInstance[DOperation](constructor)
  }

  private class DOperationsCatalogImpl() extends DOperationsCatalog {
    var categoryTree = DOperationCategoryNode()
    var operations = Map.empty[UUID, DOperationDescriptor]
    private val operationsConstructors = mutable.Map.empty[String, Constructor[_]]

    private def constructorForType(operationType: ru.Type) = {
      TypeUtils.constructorForType(operationType) match {
        case Some(x) => x
        case None => throw NoParameterlessConstructorInDOperationException(operationType)
      }
    }

    def registerDOperation[T <: DOperation : ru.TypeTag](
        id: UUID,
        category: DOperationCategory,
        description: String): Unit = {
      val operationType = ru.typeOf[T]
      val constructor = constructorForType(operationType)
      val operationInstance = DOperationsCatalog.createDOperation(constructor)
      val name = operationInstance.name
      val parameters = operationInstance.parameters
      val inPortTypes = operationInstance.inPortTypes.map(_.tpe)
      val outPortTypes = operationInstance.outPortTypes.map(_.tpe)
      val operationDescriptor = DOperationDescriptor(
          id, name, description, category, parameters, inPortTypes, outPortTypes)

      operations += id -> operationDescriptor
      categoryTree = categoryTree.addOperation(operationDescriptor, category)
      operationsConstructors(name) = constructor
    }

    def createDOperation(name: String): DOperation = operationsConstructors.get(name) match {
      case Some(constructor) => DOperationsCatalog.createDOperation(constructor)
      case None => throw DOperationNotFoundException(name)
    }
  }
}
