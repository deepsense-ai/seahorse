/**
 * Copyright (c) 2015, CodiLime, Inc.
 */

package io.deepsense.entitystorage

import akka.actor.ActorRef

object TestActorBasedEntityStorageClientFactory extends ActorBasedEntityStorageClientFactory {
  override def create(actorRef: ActorRef): EntityStorageClient = {
    new ActorBasedEntityStorageClient(actorRef)
  }
}
