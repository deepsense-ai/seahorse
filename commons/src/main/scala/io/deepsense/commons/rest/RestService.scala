/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Wojciech Jurczyk
 */

package io.deepsense.commons.rest

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
