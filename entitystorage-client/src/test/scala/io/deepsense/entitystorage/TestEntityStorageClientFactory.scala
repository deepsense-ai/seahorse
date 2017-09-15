/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Jacek Laskowski
 */
package io.deepsense.entitystorage

import akka.actor.ActorRef

object TestEntityStorageClientFactory extends EntityStorageClientFactory {
  override def create(actorRef: ActorRef): EntityStorageClient =
    new ActorBasedEntityStorageClient(actorRef)
}
