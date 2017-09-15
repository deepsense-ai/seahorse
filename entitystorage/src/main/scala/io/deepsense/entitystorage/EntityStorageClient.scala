/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Jacek Laskowski
 */
package io.deepsense.entitystorage

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.{FiniteDuration, Duration}

import akka.actor.{ActorRef, ActorSelection, ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.config.{Config, ConfigFactory}

import io.deepsense.entitystorage.api.akka.EntitiesApiActor
import io.deepsense.entitystorage.api.akka.EntitiesApiActor.{Create, Request, Get}
import io.deepsense.entitystorage.models.{InputEntity, Entity}
import io.deepsense.entitystorage.services.EntityService

trait EntityStorageClient {
  def getEntityData(tenantId: String, id: Entity.Id)
    (implicit duration: FiniteDuration): Future[Option[Entity]]
  def createEntity(inputEntity: InputEntity)
    (implicit duration: FiniteDuration): Future[Entity]
}

class ActorBasedEntityStorageClient(entitiesApiActor: ActorRef) extends EntityStorageClient {

  def getEntityData(tenantId: String, id: Entity.Id)
    (implicit duration: FiniteDuration): Future[Option[Entity]] = {
    import akka.util.Timeout._
    send(Get(tenantId, id))(durationToTimeout(duration)).mapTo[Option[Entity]]
  }

  def createEntity(inputEntity: InputEntity)
    (implicit duration: FiniteDuration): Future[Entity] = {
    import akka.util.Timeout.durationToTimeout
    send(Create(inputEntity))(duration).mapTo[Entity]
  }

  private def send(r: Request)(implicit timeout: Timeout): Future[Any] = entitiesApiActor.ask(r)
}

object EntityStorageClient {
  def apply(actorSystemName: String, hostname: String, port: Int, actorName: String,
    timeoutSeconds: Int): EntityStorageClient = {
    val actorSystem = ActorSystem(s"$actorSystemName-client")
    val path = s"akka.tcp://$actorSystemName@$hostname:$port/user/$actorName"

    import scala.concurrent.duration._
    implicit val timeout = Timeout(timeoutSeconds.seconds)

    val actorRef =
      Await.result(actorSystem.actorSelection(path).resolveOne(), timeoutSeconds.seconds)
    new ActorBasedEntityStorageClient(actorRef)
  }
}
