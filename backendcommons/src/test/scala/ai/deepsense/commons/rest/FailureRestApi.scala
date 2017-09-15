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

import spray.http.MediaTypes._
import spray.http.{HttpCharsets, MultipartFormData}
import spray.httpx._
import spray.httpx.unmarshalling._
import spray.json._
import spray.routing._
import spray.routing.directives.BasicDirectives

import ai.deepsense.commons.auth.directives.AbstractAuthDirectives
import ai.deepsense.commons.auth.usercontext.UserContext
import ai.deepsense.commons.exception.{DeepSenseException, FailureCode}

class FailureRestApi(implicit ec: ExecutionContext)
  extends RestComponent
  with RestApiAbstractAuth
  with Directives
  with DefaultJsonProtocol
  with SprayJsonSupport
  with MockAuthDirectives {

  val TestEntityReaderNullPointer = new RootJsonReader[TestEntity] {
    override def read(json: JsValue): TestEntity = {
      throw new NullPointerException("Test NPE")
    }
  }

  val TestEntityReaderDeepsenseException = new RootJsonReader[TestEntity] {
    override def read(json: JsValue): TestEntity = {
      throw TestDeepSenseException()
    }
  }

  val TestEntityReaderDeserializationException = new RootJsonReader[TestEntity] {
    override def read(json: JsValue): TestEntity = {
      throw new DeserializationException("Deserialization message", TestDeepSenseException())
    }
  }

  val TestEntityReaderDeserializationOK = new RootJsonReader[TestEntity] {
    override def read(json: JsValue): TestEntity = {
      val print1: String = json.asJsObject("NOT AN OBJECT").prettyPrint
      new TestEntity(print1)
    }
  }

  val partName = "testFile"

  def multipartUnmarshaller[T](reader: RootJsonReader[T]): Unmarshaller[T] = {
    Unmarshaller.delegate[MultipartFormData, T](`multipart/form-data`) {
      case multipartFormData =>
        val stringData = selectFormPart(multipartFormData, partName)
        reader.read(JsonParser(ParserInput(stringData)))
    }
  }

  private def selectFormPart(multipartFormData: MultipartFormData, partName: String): String =
    multipartFormData.fields
      .filter(_.name.get == partName)
      .map(_.entity.asString(HttpCharsets.`UTF-8`))
      .mkString

  def route: Route = {
    handleRejections(rejectionHandler) {
      handleExceptions(exceptionHandler) {
        path("nullpointer") {
          post {
            implicit val reader = TestEntityReaderNullPointer
            entity(as[TestEntity]) { entity =>
              complete("OK")
            }
          }
        } ~
        path("deepsense") {
          post {
            implicit val reader = TestEntityReaderDeepsenseException
            entity(as[TestEntity]) { entity =>
              complete("OK")
            }
          }
        } ~
        path("deserialization") {
          post {
            implicit val reader = TestEntityReaderDeserializationException
            entity(as[TestEntity]) { entity =>
              complete("OK")
            }
          }
        } ~
        path("upload-ok") {
          post {
            implicit val reader = multipartUnmarshaller(TestEntityReaderDeserializationOK)
            entity(as[TestEntity]) { _ =>
              complete("OK")
            }
          }
        } ~
        path("upload-deepsense") {
          post {
            implicit val reader = multipartUnmarshaller(TestEntityReaderDeepsenseException)
            entity(as[TestEntity]) { _ =>
              complete("OK")
            }
          }
        } ~
        path("upload-nullpointer") {
          post {
            implicit val reader = multipartUnmarshaller(TestEntityReaderNullPointer)
            entity(as[TestEntity]) { _ =>
              complete("OK")
            }
          }
        } ~
        path("upload-deserialization") {
          post {
            implicit val reader = multipartUnmarshaller(TestEntityReaderDeserializationException)
            entity(as[TestEntity]) { _ =>
              complete("OK")
            }
          }
        }
      }
    }
  }
}

case class TestDeepSenseException()
  extends DeepSenseException(
    FailureCode.UnexpectedError,
    "Test Deepsense Exception",
    "This is a test message from a deepsense exception")

case class TestEntity(x: String) {
  require(x == "{}", "Json object cannot have a space inside")
}

trait MockAuthDirectives extends AbstractAuthDirectives {
  def withUserContext: Directive1[Future[UserContext]] =
    BasicDirectives.provide[Future[UserContext]](???)

  def withUserId: Directive1[Future[UserContext]] =
    BasicDirectives.provide[Future[UserContext]](???)
}


