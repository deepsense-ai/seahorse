/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager.service

import akka.actor.{ActorRef, ActorSystem}
import com.google.inject.name.Named
import com.google.inject.{AbstractModule, Provides, Singleton}

import io.deepsense.commons.akka.GuiceAkkaExtension
import io.deepsense.sessionmanager.service.actors.SessionServiceActor
import io.deepsense.sessionmanager.service.sessionspawner.SessionSpawner
import io.deepsense.sessionmanager.service.sessionspawner.sparklauncher.SparkLauncherSessionSpawner
import io.deepsense.sessionmanager.service.sessionspawner.sparklauncher.auth._
import io.deepsense.sessionmanager.service.statusinferencer.DefaultStatusInferencer

class ServiceModule extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[SessionSpawner]).to(classOf[SparkLauncherSessionSpawner])
    bind(classOf[StatusInferencer]).to(classOf[DefaultStatusInferencer])
  }

  @Provides
  @Singleton
  @Named("SessionService.Actor")
  def sessionServiceActor(system: ActorSystem): ActorRef = {
    system.actorOf(GuiceAkkaExtension(system).props[SessionServiceActor], "SessionService.Actor")
  }

  @Provides
  @Singleton
  def authContextInitiator(
      @Named("launcher.auth") launcherAuth: String): AuthContextInitiator = {
    launcherAuth match {
      case "kerberos" => new KerberosInitiator
      case _ => new DummyAuthContextInitiator
    }
  }
}
