/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 *  Owner: Rafal Hryciuk
 */

package io.deepsense.entitystorage

import com.google.inject.Guice

import io.deepsense.commons.rest.RestServer

/**
 * This is the entry point of the Entity Storage application.
 */
object EntityStorageApp extends App {
  val injector = Guice.createInjector(new EntityStorageAppModule)

  injector.getInstance(classOf[RestServer]).start()
}
