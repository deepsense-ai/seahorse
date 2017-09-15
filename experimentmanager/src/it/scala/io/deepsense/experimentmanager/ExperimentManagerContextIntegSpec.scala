/**
 * Copyright (c) 2015, CodiLime, Inc.
 */

package io.deepsense.experimentmanager

import com.google.inject.Guice
import org.scalatest.FlatSpec

import io.deepsense.deeplang.catalogs.doperable.DOperableCatalog
import io.deepsense.experimentmanager.rest.OperationsApi

class ExperimentManagerContextIntegSpec extends FlatSpec {

  val injector = Guice.createInjector(new ExperimentManagerAppModule)

  "Guice context" should "have all needed services created" in {
    checkIfSingleton(classOf[DOperableCatalog])
    assert(injector.getInstance(classOf[OperationsApi]) != null)
  }

  private def checkIfSingleton[T <: AnyRef](clazz: Class[T]) = {
    val instance1: T = injector.getInstance(clazz)
    val instance2: T = injector.getInstance(clazz)
    assert(instance1 eq instance2)
  }
}
