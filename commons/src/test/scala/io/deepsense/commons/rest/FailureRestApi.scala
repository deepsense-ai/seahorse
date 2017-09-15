/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.commons.rest

import scala.concurrent.{ExecutionContext, Future}

import spray.http.MediaTypes._
import spray.http.{HttpCharsets, MultipartFormData}
import spray.httpx._
import spray.httpx.unmarshalling._
import spray.json._
import spray.routing._
import spray.routing.directives.BasicDirectives

import io.deepsense.commons.auth.directives.AbstractAuthDirectives
import io.deepsense.commons.auth.usercontext.UserContext
import io.deepsense.commons.exception.{DeepSenseException, FailureCode}

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
}


