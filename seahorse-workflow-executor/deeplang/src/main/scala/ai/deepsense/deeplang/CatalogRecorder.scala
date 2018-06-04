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

package ai.deepsense.deeplang

import java.io.File
import java.net.{URL, URLClassLoader}

import ai.deepsense.commons.utils.LoggerForCallerClass
import ai.deepsense.deeplang.catalogs.DCatalog
import ai.deepsense.deeplang.catalogs.spi.CatalogRegistrant
import ai.deepsense.deeplang.catalogs.spi.CatalogRegistrar.DefaultCatalogRegistrar
import ai.deepsense.deeplang.refl.CatalogScanner
import org.apache.spark.SparkContext

/**
 * Class used to register all desired DOperables and DOperations.
 */
class CatalogRecorder private (jars: Seq[URL]) {
  def withDir(jarsDir: File): CatalogRecorder = {
    val additionalJars =
      if (jarsDir.exists && jarsDir.isDirectory) {
        jarsDir.listFiles().toSeq.filter(f => f.isFile && f.getName.endsWith(".jar"))
      } else {
        Seq.empty
      }
    withJars(additionalJars)
  }

  def withJars(additionalJars: Seq[File]): CatalogRecorder = {
    new CatalogRecorder(jars ++ additionalJars.map(_.toURI.toURL))
  }

  def withSparkContext(sparkContext: SparkContext): CatalogRecorder = {
    new CatalogRecorder(jars ++ sparkContext.jars.map(new URL(_)))
  }

  lazy val catalogs: DCatalog = {
    val registrar = new DefaultCatalogRegistrar()
    val loader = URLClassLoader.newInstance(jars.toArray, getClass.getClassLoader)
    CatalogRegistrant.load(registrar, loader)
    new CatalogScanner(jars).register(registrar)
    registrar.catalog
  }
}

object CatalogRecorder {
  val logger = LoggerForCallerClass()

  def fromDir(dir: File): CatalogRecorder = {
    new CatalogRecorder(Seq.empty).withDir(dir)
  }
  def fromJars(jars: Seq[URL]): CatalogRecorder = {
    new CatalogRecorder(jars)
  }
  def fromSparkContext(sparkContext: SparkContext): CatalogRecorder = {
    new CatalogRecorder(Seq.empty).withSparkContext(sparkContext)
  }

  lazy val resourcesCatalogRecorder: CatalogRecorder = {
    fromDir(Config.jarsDir)
  }
}
