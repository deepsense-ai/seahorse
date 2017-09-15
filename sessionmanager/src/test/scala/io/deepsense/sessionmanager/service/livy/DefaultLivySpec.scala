/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager.service.livy

import akka.actor.ActorSystem
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.core.WireMockConfiguration._
import org.mockito.Mockito
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterEach
import org.scalatest.time.{Milliseconds, Span}
import spray.http.StatusCodes
import spray.json._

import io.deepsense.commons.models.Id
import io.deepsense.commons.{StandardSpec, UnitTestSupport}
import io.deepsense.sessionmanager.service.livy.requests.Create
import io.deepsense.sessionmanager.service.livy.responses.BatchState.BatchState
import io.deepsense.sessionmanager.service.livy.responses.{Batch, BatchState}

class DefaultLivySpec
  extends StandardSpec
    with UnitTestSupport
    with BeforeAndAfterEach {

  val defaultTimeout = 5000 // 5 sec
  implicit val patience = PatienceConfig(timeout = Span(defaultTimeout, Milliseconds))

  val httpHost = "localhost"
  var httpPort: Int = _

  val wireMockServer = new WireMockServer(wireMockConfig().port(0))
  val testSystem = ActorSystem("LivySpec")

  val requestBuilder = mock[RequestBodyBuilder]

  override def beforeAll(): Unit = {
    wireMockServer.start()
    httpPort = wireMockServer.port()
    WireMock.configureFor(httpHost, httpPort)
  }

  override def beforeEach(): Unit = {
    WireMock.reset()
    Mockito.reset(requestBuilder)
  }

  override def afterAll(): Unit = {
    wireMockServer.stop()
  }

  "DefaultLivy" when {
    "getting a non existing session" should {
      "return None" in {
        stubFor(get(urlEqualTo("/batches/123"))
          .willReturn(aResponse()
            .withStatus(StatusCodes.NotFound.intValue)
            .withHeader("Content-Type", "application/json; charset=UTF-8")))

        whenReady(createTestLivy.getSession(123)) {
          _ shouldBe None
        }
      }
    }
    "getting an existing session" should {
      "return the requested session" in {
        val status = BatchState.Running
        val id: Int = 123
        stubFor(get(urlEqualTo(s"/batches/$id"))
          .willReturn(aResponse()
            .withStatus(StatusCodes.OK.intValue)
            .withHeader("Content-Type", "application/json; charset=UTF-8")
            .withBody(sessionJson(id, status).compactPrint)))

        whenReady(createTestLivy.getSession(id)) {
          _ shouldBe Some(Batch(id, status))
        }
      }
    }

    "listing sessions" should {
      "return a list of sessions" in {
        stubFor(get(urlEqualTo(s"/batches"))
          .willReturn(aResponse()
            .withStatus(StatusCodes.OK.intValue)
            .withHeader("Content-Type", "application/json; charset=UTF-8")
            .withBody(sessionsJson.compactPrint)))

        whenReady(createTestLivy.listSessions()) {
          _.sessions should contain theSameElementsAs sessionsObjects
        }
      }
    }

    "killing a session" should {
      "call an appropriate url" in {
        val id: Int = 123
        whenReady(createTestLivy.killSession(id)) { _ =>
          verify(deleteRequestedFor(urlEqualTo(s"/batches/$id")))
        }
      }
      "return false if no job was killed" in {
        val id: Int = 123
        stubFor(delete(urlEqualTo(s"/batches/$id"))
          .willReturn(aResponse()
            .withStatus(StatusCodes.NotFound.intValue)
            .withHeader("Content-Type", "application/json; charset=UTF-8")))

        whenReady(createTestLivy.killSession(id))(_ shouldBe false)
      }
      "return true if a job was killed" in {
        val id: Int = 123
        stubFor(delete(urlEqualTo(s"/batches/$id"))
          .willReturn(aResponse()
            .withStatus(StatusCodes.OK.intValue)
            .withHeader("Content-Type", "application/json; charset=UTF-8")))

        whenReady(createTestLivy.killSession(id))(_ shouldBe true)
      }
    }

    "creating a session" should {
      "return the created session" in {
        val workflowId: Id = Id.randomId
        val unimportantCreate = Create("", "", Seq.empty, Seq.empty, Map.empty)
        when(requestBuilder.createSession(workflowId)).thenReturn(unimportantCreate)
        val returnedId = 123
        stubFor(post(urlEqualTo(s"/batches"))
          .willReturn(aResponse()
            .withStatus(StatusCodes.Created.intValue)
            .withHeader("Content-Type", "application/json; charset=UTF-8")
            .withBody(sessionJson(returnedId, BatchState.Running).compactPrint)))

        whenReady(createTestLivy.createSession(workflowId))(_.id shouldBe returnedId)
      }
      "call an appropriate url" in {
        val workflowId: Id = Id.randomId
        val (stringRequest, objectRequest) = createSessionRequest(workflowId)
        when(requestBuilder.createSession(workflowId)).thenReturn(objectRequest)
        createTestLivy.createSession(workflowId)
        verify(postRequestedFor(urlEqualTo("/batches"))
          .withHeader("Content-Type", equalTo("application/json; charset=UTF-8"))
          .withRequestBody(equalToJson(stringRequest)))
      }
    }
  }

  private def createSessionRequest(workflowId: Id): (String, Create) = {
    val c = Create(
      "mock file",
      "mock class name",
      Seq("arg1", "arg2", "arg3"),
      Seq("jar1", "jar2"),
      Map("key1" -> "val1", "key2" -> "val2")
    )

    val json = JsObject(
      "file" -> JsString(c.file),
      "className" -> JsString(c.className),
      "args" -> JsArray(c.args.map(JsString(_)): _*),
      "jars" -> JsArray(c.jars.map(JsString(_)): _*),
      "conf" -> JsObject(c.conf.mapValues(JsString(_)))
    )

    (json.compactPrint, c)
  }


  private def createTestLivy: Livy = {
    new DefaultLivy(testSystem, defaultTimeout, s"http://$httpHost:$httpPort", requestBuilder)
  }


  private val (sessionsJson, sessionsObjects) = {
    val batches = Seq(
      Batch(3, BatchState.Ok),
      Batch(1, BatchState.Error),
      Batch(4, BatchState.Idle))

    val json = JsObject(
      "sessions" -> JsArray(batches.map(b => sessionJson(b.id, b.state)): _*)
    )

    (json, batches)
  }

  private def sessionJson(id: Int, state: BatchState): JsObject = JsObject(
    "id" -> JsNumber(id),
    "state" -> JsString(state.toString)
  )
}
