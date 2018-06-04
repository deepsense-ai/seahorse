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

package ai.deepsense.workflowmanager.deeplang

import com.google.inject.{AbstractModule, Provides, Singleton}

import ai.deepsense.deeplang.CatalogRecorder
import ai.deepsense.deeplang.catalogs.doperable.DOperableCatalog
import ai.deepsense.deeplang.catalogs.doperations.DOperationsCatalog

/**
 * Provides DOperableCatalog and DOperationsCatalog.
 *
 * This module is just to satisfy dependencies. It should
 * be changed to provide valid catalogs as currently no
 * objects are registered in them.
 */
class DeepLangModule extends AbstractModule {

  override def configure(): Unit = {

  }

  @Singleton
  @Provides
  def provideDOperationsCatalog(): DOperationsCatalog = {
    CatalogRecorder.resourcesCatalogRecorder.catalogs.operations
  }

  @Singleton
  @Provides
  def provideDOperablesCatalog(): DOperableCatalog = {
    CatalogRecorder.resourcesCatalogRecorder.catalogs.operables
  }
}
