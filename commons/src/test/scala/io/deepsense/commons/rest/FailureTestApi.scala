/**
 * Copyright (c) 2015, CodiLime, Inc.
 */

package io.deepsense.commons.rest

import scala.concurrent.{ExecutionContext, Future}

import spray.routing.Directives

/**
 * A simple REST API that uses a failing service.
 * Its purpose is to test how failures in RestComponents are
 * handled in RestService.
 */
class FailureTestApi(implicit ec: ExecutionContext)
  extends RestComponent
  with Directives {
  val service = new FailureTestService

  def route = {
    path("echo") {
      get {
        parameter('msg) { (required) =>
          complete(required)
        }
      }
    } ~
    path("timeout") {
      get {
        complete {
          service.timeout(5000)
        }
      }
    } ~
    path("crash") {
      get {
        complete {
          service.crash("crash boom bang")
        }
      }
    } ~
    path("exception") {
      get {
        parameters('code.?.as[Option[Int]]) { (code) =>
          complete {
            service.fail(
              code match {
                case Some(c) => new ExceptionWithStatus(c, "exceptionWithStatus")
                case None => new Exception("arbitrary exception")
              }
            )
          }
        }
      }
    }
  }
}

class FailureTestService {
  def fail(th: Throwable)(implicit ec: ExecutionContext): Future[String] = {
    Future {
      throw th
    }
  }

  def crash(message: String)(implicit ec: ExecutionContext): Future[String] = {
    Future {
      sys.error(s"crash: $message")
      message
    }
  }

  def timeout(millis: Long)(implicit ec: ExecutionContext): Future[String] = {
    Future {
      Thread.sleep(millis)
      "succeeded after timeout"
    }
  }
}
