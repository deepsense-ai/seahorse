/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager.service

import akka.actor.{ActorRef, ActorSystem}
import com.google.inject.name.Named
import com.google.inject.{AbstractModule, Provides, Singleton}

import io.deepsense.commons.akka.GuiceAkkaExtension
import io.deepsense.sessionmanager.service.actors.SessionServiceActor
import io.deepsense.sessionmanager.service.livy.{DefaultLivy, Livy, RequestBodyBuilder, SessionExecutorRequestBodyBuilder}
import io.deepsense.sessionmanager.service.statusinferencer.DefaultStatusInferencer

class ServiceModule extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[Livy]).to(classOf[DefaultLivy])
    bind(classOf[RequestBodyBuilder]).to(classOf[SessionExecutorRequestBodyBuilder])
    bind(classOf[StatusInferencer]).to(classOf[DefaultStatusInferencer])
  }

  @Provides
  @Singleton
  @Named("SessionService.Actor")
  def sessionServiceActor(system: ActorSystem): ActorRef = {
    system.actorOf(GuiceAkkaExtension(system).props[SessionServiceActor], "SessionService.Actor")
  }
}
