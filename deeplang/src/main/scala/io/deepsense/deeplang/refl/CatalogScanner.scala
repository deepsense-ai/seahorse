/**
 * Copyright 2016, deepsense.io
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

package io.deepsense.deeplang.refl

import scala.collection.JavaConversions._
import scala.reflect.runtime.{universe => ru}

import org.reflections.Reflections

import io.deepsense.commons.utils.Logging
import io.deepsense.deeplang.catalogs.doperable.DOperableCatalog
import io.deepsense.deeplang.catalogs.doperations.DOperationsCatalog
import io.deepsense.deeplang.{DOperable, DOperation, DOperationCategories, TypeUtils}

object CatalogScanner extends Logging {

  private def scanForRegistrables() : Set[Class[_]] = {
    val reflections = new Reflections()
    reflections.getTypesAnnotatedWith(classOf[Register]).toSet
  }

  private def filterRegistrables[T](
    clazz: Class[T],
    registrables: Traversable[Class[_]]
  ): Traversable[Class[T]] = {
    registrables.filter(clazz.isAssignableFrom).map(_.asInstanceOf[Class[T]])
  }

  private def register[T](
    registrables: Traversable[Class[T]],
    performRegistration: ru.Type => Any
  ): Unit = {
    for (r <- registrables) {
      logger.debug(s"Registering ${r.getCanonicalName}")
      performRegistration(TypeUtils.classToType(r))
    }
  }

  private def registerDOperations(
    catalog: DOperationsCatalog,
    operations: Traversable[Class[DOperation]]
  ): Unit = {
    register(operations,
      catalog.registerDOperation(_, DOperationCategories.UserDefined))
  }

  private def registerDOperables(
    catalog: DOperableCatalog,
    operables: Traversable[Class[DOperable]]
  ): Unit = {
    register(operables, catalog.register)
  }

  /**
    * Scans jars on classpath for classes annotated with [[io.deepsense.deeplang.refl.Register Register]]
    * annotation and at the same time implementing [[io.deepsense.deeplang.DOperation DOperation]]
    * or [[io.deepsense.deeplang.DOperable DOperable]] interfaces. Found classes are then registered
    * in appropriate catalogs.
    *
    * @param dOperableCatalog [[io.deepsense.deeplang.DOperable DOperable]]s catalog
    * @param dOperationsCatalog [[io.deepsense.deeplang.DOperation DOperation]]s catalog
    *
    * @see [[io.deepsense.deeplang.refl.Register Register]]
    */
  def scanAndRegister(
    dOperableCatalog: DOperableCatalog,
    dOperationsCatalog: DOperationsCatalog
  ): Unit = {
    val registrables = scanForRegistrables()
    val operations = filterRegistrables(classOf[DOperation], registrables)
    val operables = filterRegistrables(classOf[DOperable], registrables)

    if (registrables.size != operations.size + operables.size) {
      logger.warn("Found annotated classes which " +
        "do not implement required interfaces")
    }

    registerDOperables(dOperableCatalog, operables)
    registerDOperations(dOperationsCatalog, operations)
  }

}
