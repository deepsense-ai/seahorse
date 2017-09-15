/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 *  Owner: Rafal Hryciuk
 */

package io.deepsense.entitystorage

import akka.actor.ActorSystem
import com.google.inject.Guice

import io.deepsense.commons.rest.RestServer

/**
 * This is the entry point of the Entity Storage application.
 */
object EntityStorageApp extends App {
  val insecure: Boolean = args.headOption.map("insecure".equalsIgnoreCase(_)).getOrElse(false)
  val injector = Guice.createInjector(new EntityStorageAppModule(insecure))
  injector.getInstance(classOf[RestServer]).start()
  injector.getInstance(classOf[ActorSystem]).awaitTermination()
  System.exit(1)
}
