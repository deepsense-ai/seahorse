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

package ai.deepsense.commons

import scala.collection.JavaConversions.asScalaSet
import scala.concurrent.duration._
import scala.reflect.{ClassTag, classTag}

import _root_.akka.actor.ActorRefFactory
import com.google.inject.{AbstractModule, Guice, Module, Provides}
import com.typesafe.config.Config
import org.scalatest.concurrent.IntegrationPatience

import ai.deepsense.commons.rest.{RestComponent, RestService}


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
     * [[ai.deepsense.commons.rest.RestServiceActor]]
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
