/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.workflowmanager

import com.google.inject.Guice

import io.deepsense.commons.StandardSpec
import io.deepsense.deeplang.catalogs.doperable.DOperableCatalog
import io.deepsense.workflowmanager.rest.{InsecureOperationsApi, InsecureWorkflowApi}

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
