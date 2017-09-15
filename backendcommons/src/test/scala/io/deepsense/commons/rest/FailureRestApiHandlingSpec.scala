/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.commons.rest

import akka.actor.ActorSystem
import spray.http.HttpEntity.NonEmpty
import spray.http.MediaTypes._
import spray.http._
import spray.httpx.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, JsObject}
import spray.testkit.ScalatestRouteTest

import io.deepsense.commons.StandardSpec

class FailureRestApiHandlingSpec
  extends StandardSpec
  with RestService
  with SprayJsonSupport
  with DefaultJsonProtocol
  with ScalatestRouteTest {
  override def actorRefFactory: ActorSystem = system

  override def apis: Seq[RestComponent] = {
    Seq(new FailureRestApi()(executor))
  }

  val testEntity: HttpEntity = HttpEntity(`application/json`, """{ "foo": "bar" }""")

  "RestApi" should {
    "answer BadRequest with Json error description" when {
      "entity json reader throws runtime exception" in {
        Post("/nullpointer", testEntity) ~> sealRoute(standardRoute) ~> check {
          shouldBeInternalServerError()
        }
      }
      "entity json reader throws deepsense exception" in {
        Post("/deepsense", testEntity) ~> sealRoute(standardRoute) ~> check {
          status should be(StatusCodes.BadRequest)
          shouldBeDeepsenseExceptionDescription()
        }
      }
      "entity json reader throws deserialization exception" in {
        Post("/deserialization", testEntity) ~> sealRoute(standardRoute) ~> check {
          shouldBeDeserializationExceptionDescription()
        }
      }
      "uploaded file is not JSON and cannot be deserialized" in {
        Post("/upload-ok", uploadFile("{; ; not json")) ~> sealRoute(standardRoute) ~> check {
          status should be(StatusCodes.BadRequest)
          shouldBeFailureDescription(
            responseAs[JsObject],
            code = Some("UnexpectedError"),
            title = Some("Malformed request"),
            message = Some("The request content does not seem to be JSON"))
        }
      }
      "uploaded entity's requirement failed" in {
        Post("/upload-ok", uploadFile("{     }")) ~> sealRoute(standardRoute) ~> check {
          shouldBeDeserializationExceptionDescription()
        }
      }
      "uploaded file reader throws deepsense exception" in {
        Post("/upload-deepsense", uploadFile()) ~> sealRoute(standardRoute) ~> check {
          shouldBeDeepsenseExceptionDescription()
        }
      }
      "uploaded file reader throws deserialization exception" in {

        Post("/upload-deserialization", uploadFile()) ~> sealRoute(standardRoute) ~> check {
          shouldBeDeserializationExceptionDescription()
        }
      }
      "uploaded file reader throws null exception" in {
        Post("/upload-nullpointer", uploadFile()) ~> sealRoute(standardRoute) ~> check {
          shouldBeInternalServerError()
        }
      }
    }
  }

  def uploadFile(data: String): MultipartFormData = uploadFile(Some(data))
  def uploadFile(data: Option[String] = None): MultipartFormData = {
    val httpEntity = HttpEntity(
      MediaTypes.`multipart/form-data`,
      HttpData(data.getOrElse("{}"))
    ).asInstanceOf[NonEmpty]
    val formFile = FormFile("testFile", httpEntity)
    val mfd = MultipartFormData(Seq(BodyPart(formFile, "testFile")))
    mfd
  }

  def shouldBeDeserializationExceptionDescription(): Unit = {
    status should be(StatusCodes.BadRequest)
    shouldBeFailureDescription(
      responseAs[JsObject],
      code = Some("UnexpectedError"),
      title = Some("Malformed request"),
      message = Some("The request content was malformed:"))
  }

  def shouldBeDeepsenseExceptionDescription(): Unit = {
    status should be(StatusCodes.BadRequest)
    shouldBeFailureDescription(
      responseAs[JsObject],
      code = Some("UnexpectedError"),
      title = Some("Test Deepsense Exception"),
      message = Some("This is a test message from a deepsense exception"))
  }

  def shouldBeInternalServerError(): Unit = {
    status should be(StatusCodes.InternalServerError)
    shouldBeFailureDescription(
      responseAs[JsObject],
      code = Some("UnexpectedError"),
      title = Some("Internal Server Error"),
      message = Some("The request could not be processed because of internal server error:"))
  }

  def shouldBeFailureDescription(
      json: JsObject,
      id: Option[String] = None,
      code: Option[String] = None,
      title: Option[String] = None,
      message: Option[String] = None): Unit = {
    json.fields should contain key "id"
    json.fields should contain key "code"
    json.fields should contain key "message"
    json.fields should contain key "details"
    json.fields should contain key "title"

    def check(value: Option[String], key: String) = {
      value.foreach { v =>
        json.fields(key).convertTo[String] should startWith (v)
      }
    }

    check(id, "id")
    check(code, "code")
    check(message, "message")
    check(title, "title")
  }
}
