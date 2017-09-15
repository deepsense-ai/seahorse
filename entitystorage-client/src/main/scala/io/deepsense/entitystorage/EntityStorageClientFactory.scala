/**
 * Copyright (c) 2015, CodiLime, Inc.
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

case class EntityStorageClientFactoryImpl(
    host: String = "localhost",
    port: Int = 0)
  extends EntityStorageClientFactory {

  import scala.collection.JavaConversions._

  val actorSystem = ActorSystem(
    "EntityStorageClient",
    ConfigFactory.parseMap(
      Map(
        "akka.actor.provider" -> "akka.remote.RemoteActorRefProvider",
        "akka.remote.netty.tcp.port" -> port,
        "akka.remote.hostname" -> host
      )
    )
  )

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
