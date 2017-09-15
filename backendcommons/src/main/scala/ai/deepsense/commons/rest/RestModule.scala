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

package ai.deepsense.commons.rest

import scala.concurrent.Await
import scala.concurrent.duration._

import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.ask
import akka.util.Timeout
import com.google.inject.name.Named
import com.google.inject.{AbstractModule, Provides, Singleton}

import ai.deepsense.commons.akka.GuiceAkkaExtension

/**
 * Configures RestServer internals.
 */
class RestModule extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[RestServer])
  }

  @Provides
  @Singleton
  @Named("ApiRouterActorRef")
  def provideApiRouterActorRef(
      @Named("server.startup.timeout") startupTimeout: Long,
      system: ActorSystem): ActorRef = {
    val supervisor =
      system.actorOf(GuiceAkkaExtension(system).props[RestServiceSupervisor], "RestSupervisor")
    val restServiceActorProps = GuiceAkkaExtension(system).props[RestServiceActor]
    implicit val timeout: Timeout = startupTimeout.seconds
    val actorRef = supervisor.ask((restServiceActorProps, "RestServiceActor")).mapTo[ActorRef]
    Await.result(actorRef, timeout.duration)
  }
}
