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

import scala.collection.JavaConversions.asScalaSet

import akka.actor.{Actor, ActorContext}
import com.google.inject.Inject
import spray.http.StatusCodes
import spray.routing._
import spray.util.LoggingContext

/**
 * Akka actor responsible for handling REST API requests.
 */
class RestServiceActor @Inject()(apiSet: java.util.Set[RestComponent])
  extends Actor
  with RestService {
  def actorRefFactory: ActorContext = {
    context
  }

  def receive: Receive = {
    runRoute(standardRoute)
  }

  protected[this] def apis = asScalaSet(apiSet).toSeq
}

trait RestService extends HttpService {
  /**
   * @return List of apis to include in route
   */
  protected[this] def apis: Seq[RestComponent]

  lazy val standardRoute: Route =
    handleRejections(rejectionHandler) {
      handleExceptions(exceptionHandler) {
        apis.tail.foldLeft(apis.head.route) { (chain, next) =>
          chain ~ next.route
        }
      }
    }

  /**
   * Override default to respond with 503 rather than 500
   */
  override def timeoutRoute: Route = {
    complete(
      StatusCodes.ServiceUnavailable,
      "The server could not provide a timely response."
    )
  }

  private def exceptionHandler(implicit log: LoggingContext): ExceptionHandler = {
    ExceptionHandler {
      case e: ExceptionWithStatus =>
        complete(e.statusCode, e.msg)
    }
  }

  private val rejectionHandler: RejectionHandler = {
    RejectionHandler {
      case MissingQueryParamRejection(param) :: _ =>
        complete(StatusCodes.BadRequest, s"Request is missing required query parameter '$param'")
    }
  }
}
