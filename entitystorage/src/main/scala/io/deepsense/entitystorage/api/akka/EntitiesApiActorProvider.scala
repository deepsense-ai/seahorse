/**
 * Copyright (c) 2015, CodiLime, Inc.
 */

package io.deepsense.entitystorage.api.akka

import akka.actor.{ActorRef, ActorSystem}
import com.google.inject._

import io.deepsense.commons.akka.GuiceAkkaExtension

class EntitiesApiActorProvider @Inject() (system: ActorSystem) extends Provider[ActorRef] {

  override def get(): ActorRef =
    system.actorOf(GuiceAkkaExtension(system).props[EntitiesApiActor], "EntitiesApiActor")
}
