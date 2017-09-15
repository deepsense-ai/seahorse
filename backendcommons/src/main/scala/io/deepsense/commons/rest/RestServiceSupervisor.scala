/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.commons.rest

import akka.actor.{Actor, ActorInitializationException, Props}

import io.deepsense.commons.utils.Logging

/**
 * An actor with a specific supervising strategy. It shutdowns the actor system when
 * there was an ActorInitializationException during child actors creation.
 *
 * The main purpose of the class is supervise initiation of the application. If the environment
 * does not meet the requirements the system should be shut down.
 */
class RestServiceSupervisor extends Actor with Logging {

  import akka.actor.OneForOneStrategy
  import akka.actor.SupervisorStrategy._

  override def receive: Receive = {
    case (props: Props, name: String) =>
      logger.debug(s"RestServiceSupervisor creates actor '$name': ${props.actorClass()}")
      sender() ! context.actorOf(props, name)
    case message => unhandled(message)
  }

  override val supervisorStrategy =
    OneForOneStrategy() {
      case exception: ActorInitializationException =>
        logger.error("An ActorInitializationException occurred! Shutting down!", exception)
        context.system.shutdown()
        Stop
    }
}
