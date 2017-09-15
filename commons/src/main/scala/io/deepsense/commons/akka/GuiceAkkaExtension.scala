/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Wojciech Jurczyk
 */

package io.deepsense.commons.akka

import scala.reflect.{ClassTag, classTag}

import akka.actor.{Actor, ExtendedActorSystem, Extension, ExtensionId, ExtensionIdProvider, Props}
import com.google.inject.Injector

/**
 * Akka extension for instantiating guice injectable actors.
 *
 * Taken from: https://github.com/ehalpern/sandbox (MIT licence)
 * @author Eric Halpern (eric.halpern@gmail.com)
 */
object GuiceAkkaExtension
  extends ExtensionId[GuiceAkkaExtensionImpl]
  with ExtensionIdProvider {
  /**
   * @return the canonical extension id
   */
  override def lookup(): GuiceAkkaExtension.type = {
    GuiceAkkaExtension
  }

  /**
   * Creates a new extension instance
   */
  override def createExtension(system: ExtendedActorSystem): GuiceAkkaExtensionImpl = {
    new GuiceAkkaExtensionImpl
  }
}

/**
 * Akka extension implementation
 */
class GuiceAkkaExtensionImpl extends Extension {
  private var injector: Injector = _

  /**
   * Initializes the extension with the guice injector.  This must be called before
   * the extension can be used.
   *
   * This method is invoked indirectly using
   * {{{
   *   GuiceAkkaExtension(system).initialize(injector))
   * }}}
   */
  def initialize(injector: Injector): Unit = {
    this.injector = injector
  }

  /**
   * Returns a Props object that specifies the producer class and parameters required
   * to instantiate the given actor using the GuiceActorProducer.
   *
   * This method is invoked indirectly using
   * {{{
   *   GuiceAkkaExtension(system).props[MyActor]
   * }}}
   */
  def props[A <: Actor : ClassTag]: Props = {
    val producerClz = classTag[GuiceActorProducer[A]].runtimeClass
    val actorClz = classTag[A].runtimeClass
    Props(producerClz, injector, actorClz)
  }
}

