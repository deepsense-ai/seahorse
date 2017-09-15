/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Wojciech Jurczyk
 */

package io.deepsense.experimentmanager.akka

import akka.actor.{Actor, IndirectActorProducer}
import com.google.inject.Injector

/**
 * Allows akka to instantiate guice injected instances
 *
 * Taken from: https://github.com/ehalpern/sandbox (MIT licence)
 * @author Eric Halpern (eric.halpern@gmail.com)
 */
class GuiceActorProducer[A <: Actor](injector: Injector, actorClz: Class[A])
  extends IndirectActorProducer {
  override def actorClass: Class[A] = {
    actorClz
  }

  override def produce(): A = {
    injector.getInstance(actorClass)
  }
}
