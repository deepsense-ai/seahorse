/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Jacek Laskowski
 */
package io.deepsense.entitystorage

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{Await, Future}

import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.config.ConfigFactory

import io.deepsense.models.entities.{Entity, InputEntity}
import io.deepsense.models.protocols.EntitiesApiActorProtocol.{Create, Get, Request}

trait EntityStorageClient {
  def getEntityData(tenantId: String, id: Entity.Id)
                   (implicit duration: FiniteDuration): Future[Option[Entity]]

  def createEntity(inputEntity: InputEntity)
                  (implicit duration: FiniteDuration): Future[Entity]
}

class ActorBasedEntityStorageClient(entitiesApiActor: ActorRef) extends EntityStorageClient {

  def getEntityData(tenantId: String, id: Entity.Id)
                   (implicit duration: FiniteDuration): Future[Option[Entity]] = {
    send(Get(tenantId, id))(duration).mapTo[Option[Entity]]
  }

  def createEntity(inputEntity: InputEntity)
                  (implicit duration: FiniteDuration): Future[Entity] = {
    send(Create(inputEntity))(duration).mapTo[Entity]
  }

  private def send(r: Request)(implicit timeout: Timeout): Future[Any] = entitiesApiActor.ask(r)
}

object EntityStorageClient {
  def apply(actorSystemName: String, hostname: String, port: Int, actorName: String,
            timeoutSeconds: Int): EntityStorageClient = {
    val actorSystem = ActorSystem(s"$actorSystemName-client",
      ConfigFactory.load("entitystorage-communication.conf"))
    val path = s"akka.tcp://$actorSystemName@$hostname:$port/user/$actorName"

    import scala.concurrent.duration._
    implicit val timeout = Timeout(timeoutSeconds.seconds)

    val actorRef =
      Await.result(actorSystem.actorSelection(path).resolveOne(), timeoutSeconds.seconds)
    new ActorBasedEntityStorageClient(actorRef)
  }
}
