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

import ai.deepsense.commons.akka.AkkaModule
import ai.deepsense.commons.config.ConfigModule
import ai.deepsense.commons.jclouds.{KeystoneApiModule, TokenApiModule}
import ai.deepsense.commons.rest.RestModule

/**
 * The main module for Workflow Manager. Installs all needed modules to run
 * the application.
 */
class WorkflowManagerAppModule(withMockedSecurity: Boolean) extends AbstractModule {
  override def configure(): Unit = {
    installCore()
    installServices()
    installServer()
  }

  private def installCore(): Unit = {
    install(new ConfigModule)
    install(new AkkaModule)
    install(new KeystoneApiModule)
    install(new TokenApiModule)
  }

  private def installServices(): Unit = {
    install(new ServicesModule)
  }

  private def installServer(): Unit = {
    install(new RestModule)
    install(new ApisModule(withMockedSecurity))
  }
}
