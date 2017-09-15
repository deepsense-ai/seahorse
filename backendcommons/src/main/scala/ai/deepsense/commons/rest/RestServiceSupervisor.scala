/**
 * Copyright 2015 deepsense.ai (CodiLime, Inc)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ai.deepsense.commons.rest

import akka.actor.{Actor, ActorInitializationException, Props}

import ai.deepsense.commons.utils.Logging
import ai.deepsense.sparkutils.AkkaUtils

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
        logger.error("An ActorInitializationException occurred! Terminating!", exception)
        AkkaUtils.terminate(context.system)
        Stop
    }
}
