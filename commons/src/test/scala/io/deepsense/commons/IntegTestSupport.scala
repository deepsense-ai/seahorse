/**
 * Copyright (c) 2015, CodiLime, Inc.
 */

package io.deepsense.commons

import scala.collection.JavaConversions.asScalaSet
import scala.concurrent.duration._
import scala.reflect.{ClassTag, classTag}

import _root_.akka.actor.ActorRefFactory
import com.google.inject.{AbstractModule, Guice, Module, Provides}
import com.typesafe.config.Config
import org.scalatest.concurrent.IntegrationPatience

import io.deepsense.commons.rest.{RestComponent, RestService}


/**
 * Extends StandardSpec with features to aid integration testing including:
 *
 * - [[getRestServiceInstance]] method to obtain a fully wired application route
 * for integrated API testing
 *
 * - [[getInstance]] method to obtain a fully wired class/trait instance for
 * integrated service testing
 *
 * - longer default future and spray timeouts to accommodate the longer wait
 * times associated with using real remote services
 *
 */
trait IntegTestSupport extends IntegrationPatience {
  suite: StandardSpec =>

  protected def appGuiceModule: Module

  /**
   * An injector that creates the entire integrated object graph
   */
  private val injector = Guice.createInjector(new AbstractModule {
    override def configure(): Unit = {
      install(appGuiceModule)
    }

    /**
     * Provides the test entry point for the application router.
     * The actual application creates the router using the actor
     * [[io.deepsense.commons.rest.RestServiceActor]]
     */
    @Provides
    def provideApiRouter(
      apiSet: java.util.Set[RestComponent],
      arf: ActorRefFactory): RestService = {
      new RestService {
        implicit def actorRefFactory: ActorRefFactory = arf

        protected[this] def apis = asScalaSet(apiSet).toSeq
      }
    }
  })

  /**
   * Increases the default timeout for routing tests
   */
  override implicit protected def routeTestTimeout: RouteTestTimeout = {
    RouteTestTimeout(15.seconds)
  }

  /**
   * Retrieves an injected instance of class A
   */
  protected[this] def getInstance[A <: AnyRef : ClassTag]: A = {
    injector.getInstance(classTag[A].runtimeClass).asInstanceOf[A]
  }

  /**
   * Retrieves an injected instance of the main application route
   */
  protected[this] def getRestServiceInstance = {
    val router = getInstance[RestService]
    router.sealRoute(router.standardRoute)
  }

  protected[this] def getConfig: Config = getInstance[Config]
}
