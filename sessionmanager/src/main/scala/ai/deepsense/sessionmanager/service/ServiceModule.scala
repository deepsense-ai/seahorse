/**
 * Copyright 2016 deepsense.ai (CodiLime, Inc)
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

package ai.deepsense.sessionmanager.service

import akka.actor.{ActorRef, ActorSystem}
import com.google.inject.name.Named
import com.google.inject.{AbstractModule, Provides, Singleton}

import ai.deepsense.commons.akka.GuiceAkkaExtension
import ai.deepsense.sessionmanager.service.actors.SessionServiceActor
import ai.deepsense.sessionmanager.service.sessionspawner.SessionSpawner
import ai.deepsense.sessionmanager.service.sessionspawner.sparklauncher.SparkLauncherSessionSpawner

class ServiceModule extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[SessionSpawner]).to(classOf[SparkLauncherSessionSpawner])
  }

  @Provides
  @Singleton
  @Named("SessionService.Actor")
  def sessionServiceActor(system: ActorSystem): ActorRef = {
    system.actorOf(GuiceAkkaExtension(system).props[SessionServiceActor], "SessionService.Actor")
  }
}
