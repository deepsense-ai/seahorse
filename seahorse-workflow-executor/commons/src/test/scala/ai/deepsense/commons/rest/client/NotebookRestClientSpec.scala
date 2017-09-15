/**
 * Copyright 2016 deepsense.ai (CodiLime, Inc)
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

package ai.deepsense.commons.rest.client

import java.net.URL
import java.util.UUID

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.language.postfixOps

import akka.actor.ActorSystem
import org.scalatest.{Matchers, WordSpec}
import spray.http._

import ai.deepsense.commons.models.Id
import ai.deepsense.commons.utils.RetryActor.RetryLimitReachedException

class NotebookRestClientSpec
  extends WordSpec
  with Matchers {

  implicit val as = ActorSystem()
  val uutName: String = classOf[NotebookRestClient].getSimpleName.filterNot(_ == '$')

  private class NotebookRestClientWithMockedHttp(
      notebookServerAddress: URL,
      workflowId: Id,
      nodeId: Id,
      pollInterval: FiniteDuration,
      retryLimit: Int,
      requestsCountNeededForData: Int,
      notebookData: Array[Byte]
  ) extends NotebookRestClient(notebookServerAddress, workflowId, nodeId, pollInterval, retryLimit) {

    var nbDataRequestCount: Int = 0

    override def fetchHttpResponse(req: HttpRequest): Future[HttpResponse] = Future.successful {
      req.method match {
        case HttpMethods.GET =>
          nbDataRequestCount += 1
          if (nbDataRequestCount >= requestsCountNeededForData) {
            nbDataRequestCount = 0
            HttpResponse(entity = HttpEntity(data = HttpData(notebookData)))
          } else {
            HttpResponse(status = StatusCodes.NotFound)
          }
        case HttpMethods.POST =>
          HttpResponse()
      }
    }
  }

  private trait Setup {
    val workflowId = UUID.randomUUID()
    val nodeId = UUID.randomUUID()

    val notebookData = "This is notebook data".getBytes()
    val serverAddress = new URL("http", "localhost", 6011, "")

    def generateUUTWithHttpErrors(reqPredicate: HttpRequest => Boolean, statusCode: StatusCode): NotebookRestClient =
      new NotebookRestClientWithMockedHttp (
          serverAddress,
          workflowId,
          nodeId,
          1 nano,
          1000,
          1,
          notebookData) {
        override def fetchHttpResponse(req: HttpRequest): Future[HttpResponse] = {
          if (reqPredicate(req)) {
            Future.successful(HttpResponse(status = statusCode))
          } else {
            super.fetchHttpResponse(req)
          }
        }
      }

    def generateUUT(requestsCountNeededForData: Int, retryLimit: Int): NotebookRestClient =
      new NotebookRestClientWithMockedHttp(
        serverAddress,
        workflowId,
        nodeId,
        1 nano,
        retryLimit,
        requestsCountNeededForData,
        notebookData)

  }

  s"A $uutName" should {
    "return notebook data" when {
      "queries succeed within retry limits" in {
        new Setup {
          val uut = generateUUT(2, 5)
          Await.result(
            uut.generateAndPollNbData("python"),
            Duration.Inf) shouldBe "This is notebook data".getBytes()
        }
      }
    }

    "fail" when {
      "queries need more tries than retry limit" in {
        new Setup {
          val uut = generateUUT(4, 2 /* means 3 requests until error - 1 original and 2 retries */)
          Await.result(
            uut.generateAndPollNbData("python").failed,
            Duration.Inf
          ) shouldBe a [RetryLimitReachedException]
        }
      }

      "POST queries result in error status codes" in {
        new Setup {
          val uut = generateUUTWithHttpErrors(_.method == HttpMethods.POST, StatusCodes.NotImplemented)

          val exception = Await.result(
            uut.generateAndPollNbData("python").failed,
            Duration.Inf
          )

          exception shouldBe a [NotebookHttpException]
          exception.asInstanceOf[NotebookHttpException].httpResponse.status.intValue shouldBe 501

        }
      }

      "GET queries result in error status codes" in {
        new Setup {
          val uut = generateUUTWithHttpErrors(_.method == HttpMethods.GET, StatusCodes.InternalServerError)

          val exception = Await.result(
            uut.generateAndPollNbData("python").failed,
            Duration.Inf
          )

          exception shouldBe a [NotebookHttpException]
          exception.asInstanceOf[NotebookHttpException].httpResponse.status.intValue shouldBe 500

        }
      }
    }
  }
}
