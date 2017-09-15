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

import scala.concurrent.{ExecutionContext, Future}

import spray.routing.{Directives, Route}

/**
 * A simple REST API that uses a failing service.
 * Its purpose is to test how failures in RestComponents are
 * handled in RestService.
 */
class FailureTestApi(implicit ec: ExecutionContext)
  extends RestComponent
  with Directives {
  val service = new FailureTestService

  def route: Route = {
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
