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

import akka.actor.ActorSystem
import spray.http.StatusCodes
import spray.testkit.ScalatestRouteTest

import ai.deepsense.commons.StandardSpec

class RestServiceFailureHandlingSpec
  extends StandardSpec
  with RestService
  with ScalatestRouteTest {
  override def actorRefFactory: ActorSystem = system

  override def apis: Seq[RestComponent] = {
    Seq(new FailureTestApi()(executor))
  }

  "GET'ting a resource" should {
    "return 200 when correct parameter provided" in {
      Get("/echo?msg=hello") ~> standardRoute ~> check {
        responseAs[String] equals ("hello")
      }
    }
    "return 400 when required parameter missing" in {
      Get("/echo") ~> standardRoute ~> check {
        status equals (StatusCodes.BadRequest)
      }
    }
    "return 500 when service throw arbitrary exception" in {
      Get("/exception") ~> standardRoute ~> check {
        status equals (StatusCodes.InternalServerError)
      }
    }
    "return specified status when service throws an ExceptionWithStatus" in {
      val expected = StatusCodes.PaymentRequired
      Get(s"/exception?code=${expected.intValue}") ~> standardRoute ~> check {
        status equals (expected)
      }
    }
    "return 503 when service timed out" in {
      Get("/timeout") ~> standardRoute ~> check {
        // Ideally this test would ensure that a timed out request
        // results in a SeviceUnavailable response. Unfortunately, this
        // can't be tested without the HttpServer wrapper, since this is
        // responsible for detecting the timeout and issuing a Timedout
        // message to the actor. The best we can do here, is ensure
        // that the original request is not completed and that issuing
        // the request to the timeoutRoute results in the correct response.
        handled equals (false)
        Get("/timeout") ~> timeoutRoute ~> check {
          status equals (StatusCodes.ServiceUnavailable)
        }
      }
    }
  }
}
