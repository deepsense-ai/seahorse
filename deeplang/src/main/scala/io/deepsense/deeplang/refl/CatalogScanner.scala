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

import java.io.File
import java.net.{URL, URLClassLoader}

import scala.collection.JavaConversions._

import org.reflections.Reflections
import org.reflections.util.{ClasspathHelper, ConfigurationBuilder}

import io.deepsense.commons.utils.Logging
import io.deepsense.deeplang.catalogs.doperable.DOperableCatalog
import io.deepsense.deeplang.catalogs.doperations.DOperationsCatalog
import io.deepsense.deeplang.{DOperable, DOperation, DOperationCategories, TypeUtils}

object CatalogScanner extends Logging {

  val resourcesDirname = "/resources/jars"

  lazy val resourcesURLs: Seq[URL] = {
    val resourcesDir = new File(resourcesDirname)
    try {
      resourcesDir.listFiles().map(_.toURI.toURL)
    } catch {
      case e: Exception =>
        logger.warn(s"Couldn't list files in $resourcesDirname dir", e)
        Seq()
    }
  }

  /**
    * Scans jars on classpath for classes annotated with [[io.deepsense.deeplang.refl.Register Register]]
    * annotation and at the same time implementing [[io.deepsense.deeplang.DOperation DOperation]]
    * interface. Found classes are then registered in appropriate catalogs.
    *
    * @see [[io.deepsense.deeplang.refl.Register Register]]
    */
  def scanAndRegister(
      dOperableCatalog: DOperableCatalog,
      dOperationsCatalog: DOperationsCatalog
    ): Unit = {
    for (registrable <- scanForRegistrables()) {
      logger.debug(s"Trying to register class $registrable")
      registrable match {
        case DOperationMatcher(doperation) => registerDOperation(dOperationsCatalog, doperation)
        case other => logger.warn(s"Only DOperation can be `@Register`ed")
      }
    }
  }

  private def scanForRegistrables() : Set[Class[_]] = {
    val cl = URLClassLoader.newInstance(resourcesURLs.toArray)
    val reflections = new Reflections(
      ConfigurationBuilder.build(ClasspathHelper.forJavaClassPath())
        .addUrls(resourcesURLs: _*)
        .addClassLoader(cl)
    )
    reflections.getTypesAnnotatedWith(classOf[Register]).toSet
  }

  private def registerDOperation(
    catalog: DOperationsCatalog,
    operation: Class[DOperation]
  ): Unit = TypeUtils.constructorForClass(operation) match {
    case Some(constructor) =>
      catalog.registerDOperation(
        DOperationCategories.UserDefined,
        () => TypeUtils.createInstance[DOperation](constructor)
      )
    case None => logger.error(
      s"Class $operation could not be registered." +
        "It needs to have parameterless constructor"
    )
  }

  class AssignableFromExtractor[T](targetClass: Class[T]) {
    def unapply(clazz: Class[_]): Option[Class[T]] = {
      if (targetClass.isAssignableFrom(clazz)) {
        Some(clazz.asInstanceOf[Class[T]])
      } else {
        None
      }
    }
  }

  object DOperationMatcher extends AssignableFromExtractor(classOf[DOperation])

}
