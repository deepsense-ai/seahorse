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

package ai.deepsense.workflowmanager

import com.google.inject.AbstractModule
import com.google.inject.binder.LinkedBindingBuilder
import com.google.inject.multibindings.Multibinder
import com.google.inject.name.Names.named

import ai.deepsense.commons.auth.AuthModule
import ai.deepsense.commons.rest.{RestComponent, VersionApi}
import ai.deepsense.models.json.workflow.GraphReaderModule
import ai.deepsense.workflowmanager.deeplang.DeepLangModule
import ai.deepsense.workflowmanager.rest._

/**
 * Configures all existing APIs.
 */
class ApisModule(withMockedSecurity: Boolean) extends AbstractModule {
  private lazy val apiBinder = Multibinder.newSetBinder(binder(), classOf[RestComponent])

  protected[this] def bindApi: LinkedBindingBuilder[RestComponent] = {
    apiBinder.addBinding()
  }

  override def configure(): Unit = {
    bind(classOf[String]).annotatedWith(named("componentName")).toInstance("workflowmanager")

    install(new AuthModule(withMockedSecurity))
    install(new GraphReaderModule)
    install(new DeepLangModule)

    bindApi.to(classOf[InsecureWorkflowApi])
    bindApi.to(classOf[InsecureOperationsApi])
    bindApi.to(classOf[VersionApi])
  }
}
