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

import com.google.inject.Guice

import ai.deepsense.commons.StandardSpec
import ai.deepsense.deeplang.catalogs.doperable.DOperableCatalog
import ai.deepsense.workflowmanager.rest.{InsecureOperationsApi, InsecureWorkflowApi}

class WorkflowManagerContextIntegSpec extends StandardSpec {

  val injector = Guice.createInjector(new WorkflowManagerAppModule(withMockedSecurity = false))

  "Guice context" should {
    "have all needed services created" in {
      checkIfSingleton(classOf[DOperableCatalog])
      assert(injector.getInstance(classOf[InsecureOperationsApi]) != null)
      assert(injector.getInstance(classOf[InsecureWorkflowApi]) != null)
    }
  }

  private def checkIfSingleton[T <: AnyRef](clazz: Class[T]) = {
    val instance1: T = injector.getInstance(clazz)
    val instance2: T = injector.getInstance(clazz)
    assert(instance1 eq instance2)
  }
}
