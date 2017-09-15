/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Jacek Laskowski
 */
package io.deepsense.entitystorage

import scala.concurrent.Await

import akka.actor.{ActorSystem, ActorRef}
import akka.util.Timeout
import com.typesafe.config.ConfigFactory

trait ActorBasedEntityStorageClientFactory {
  def create(actorRef: ActorRef): EntityStorageClient
}

trait EntityStorageClientFactory {

  def create(
    actorSystemName: String,
    hostname: String,
    port: Int,
    actorName: String,
    timeoutSeconds: Int): EntityStorageClient

  /**
   * Closes EntityStorageClientFactory. After close, create cannot be executed.
   */
  def close()
}

case class EntityStorageClientFactoryImpl() extends EntityStorageClientFactory {

  val actorSystem = ActorSystem("EntityStorageClient",
    ConfigFactory.load("entitystorage-communication.conf"))

  override def create(actorSystemName: String, hostname: String, port: Int, actorName: String,
    timeoutSeconds: Int): EntityStorageClient = {
    val path = s"akka.tcp://$actorSystemName@$hostname:$port/user/$actorName"

    import scala.concurrent.duration._
    implicit val timeout = Timeout(timeoutSeconds.seconds)

    val actorRef =
      Await.result(actorSystem.actorSelection(path).resolveOne(), timeoutSeconds.seconds)
    new ActorBasedEntityStorageClient(actorRef)
  }

  /**
   * Closes EntityStorageClientFactory. After close, create cannot be executed.
   */
  override def close(): Unit = actorSystem.shutdown()
}
