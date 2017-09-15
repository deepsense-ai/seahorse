/**
  * Copyright (c) 2016, CodiLime Inc.
  */

package io.deepsense.e2etests

import java.util.UUID

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.concurrent.{Await, Future, Promise}
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.Try
import scalaz.Scalaz._
import scalaz._
import io.deepsense.sessionmanager.service.{Status => SessionStatus}

import com.ning.http.client.AsyncHttpClient
import com.ning.http.client.multipart.ByteArrayPart
import com.ning.http.client.providers.netty.NettyAsyncHttpProviderConfig
import com.ning.http.client.ws.WebSocketListener
import com.ning.http.client.{AsyncCompletionHandler, AsyncHttpClientConfig, Request}
import com.ning.http.client.{Response => AHCResponse}
import org.jfarcand.wcs.{Options, TextListener, WebSocket}
import org.scalatest.concurrent.Eventually
import org.scalatest.time.{Seconds, Span}
import org.scalatest.{Matchers, Suite}
import play.api.libs.json.{JsArray, JsObject, JsString, Json}
import play.api.libs.ws.ning.{NingWSClient, NingWSResponse}
import org.scalactic.source.Position

import io.deepsense.commons.models.ClusterDetails
import io.deepsense.commons.utils.Logging
import io.deepsense.sessionmanager.rest._
import io.deepsense.sessionmanager.rest.requests._

trait SeahorseIntegrationTestDSL extends Matchers with Eventually with Logging {

  import scala.concurrent.ExecutionContext.Implicits.global

  private val httpClient = NingWSClient()

  // Values from docker-compose file
  private val mqUser = "yNNp7VJS"
  private val mqPass = "1ElYfGNW"

  private val baseUrl = "http://localhost:33321"
  private val workflowsUrl = s"$baseUrl/v1/workflows"
  private val sessionsUrl = s"$baseUrl/v1/sessions"

  private val ws = "ws://localhost:33321/stomp/645/bg1ozddg/websocket"

  private val httpTimeout = 10 seconds
  private val workflowTimeout = 30 minutes

  private implicit val patience = PatienceConfig(
    timeout = Span(60, Seconds),
    interval = Span(10, Seconds)
  )

  def ensureSeahorseIsRunning(): Unit = {
    eventually {
      logger.info("Waiting for Seahorse to boot up...")
      val smIsUp = Await.result(httpClient.url(sessionsUrl).get, httpTimeout)
      val wmIsUp = Await.result(httpClient.url(workflowsUrl).get, httpTimeout)
    }
  }

  def getExampleWorkflowsIds(): Seq[String] = {
    Await.result(
      httpClient.url(workflowsUrl)
        .get()
        .map { response =>
          val JsArray(workflowsJson) = response.json
          val workflowJsonObjects = workflowsJson.map(_.asInstanceOf[JsObject])

          val examples = workflowJsonObjects.filter { workflow =>
            workflow.value("ownerName").asInstanceOf[JsString].value == "seahorse"
          }.sortBy { workflow =>
            workflow.value("name").asInstanceOf[JsString].value
          }

          examples.map(_.value("id").asInstanceOf[JsString].value)
        }, httpTimeout)
  }

  case class WorkflowId(workflowId: String)
  implicit val workflowIdFormat = Json.format[WorkflowId]


  def uploadWorkflow(js: String): String = {
    val asyncHttpClient = httpClient.underlying[AsyncHttpClient]
    val postBuilder = asyncHttpClient.preparePost(s"$workflowsUrl/upload")
    val builder = postBuilder.addBodyPart(new ByteArrayPart("workflowFile", js.getBytes))
    val updatedWorkflowId =
      Await.result(httpClientExecute(asyncHttpClient, builder.build())
      .map { response =>
        response.json.as[WorkflowId]
      }, httpTimeout)
    updatedWorkflowId.workflowId
  }

  private def httpClientExecute(asyncHttpClient: AsyncHttpClient,
                                request: Request) : Future[NingWSResponse] = {
    val result = Promise[NingWSResponse]()
    asyncHttpClient.executeRequest(request,
      new AsyncCompletionHandler[AHCResponse]() {
        override def onCompleted(response: AHCResponse) = {
          result.success(NingWSResponse(response))
          response
        }
        override def onThrowable(t: Throwable) = {
          result.failure(t)
        }
      })
    result.future
  }

  def cloneWorkflow(workflowId: String): String = {
    val clonedWorkflowId = Await.result(httpClient.url(s"$workflowsUrl/$workflowId/clone")
      .post(Json.obj("name" -> s"Clone of $workflowId", "description" -> "Some description"))
      .map { response =>
        response.json.asInstanceOf[JsObject].value("workflowId").asInstanceOf[JsString].value
      }, httpTimeout)
    logger.info(s"Cloned workflow with id $workflowId. Cloned workflow id $clonedWorkflowId")
    clonedWorkflowId
  }

  def deleteWorkflow(workflowId: String): Unit =
    Await.result(httpClient.url(s"$workflowsUrl/$workflowId").delete(), httpTimeout)

  case class ExecutorContext(wsClient: WebSocket, websocketMessages: mutable.MutableList[String])

  def launch(workflowId: String)(implicit ctx: ExecutorContext): Unit = {
    logger.info(s"Launching workflow $workflowId")
    ctx.wsClient.send(
      s"""["SEND\ndestination:/exchange/seahorse/workflow.$workflowId.""" +
        s"""$workflowId.from\n\n{\\"messageType\\":\\"launch\\",\\"messageBody\\"""" +
        s""":{\\"workflowId\\":\\"$workflowId\\",\\"nodesToExecute\\":[]}}\u0000"]"""
          .stripMargin)
  }

  def withExecutor[T](workflowId: String, clusterDetails: ClusterDetails)(code: ExecutorContext => T): Unit = {
    logger.info(s"Starting executor for workflow $workflowId")

    httpClient.url(sessionsUrl).post(Json.parse(createSessionBody(workflowId, clusterDetails)))

    ensureExecutorIsRunning(workflowId)
    logger.info(s"Executor for workflow $workflowId started")

    val websocketMessages = mutable.MutableList[String]()
    val wsClient = MyWebSocket().open(ws).listener(new TextListener {
      override def onMessage(message: String): Unit = {
        websocketMessages += message
      }
    })

    implicit val ctx = ExecutorContext(wsClient, websocketMessages)

    wsClient.send(
      s"""["CONNECT\nlogin:$mqUser\npasscode:$mqPass\naccept-version:1.1,1.0\n
          |heart-beat:0,0\n\n\u0000"]""".stripMargin)

    ensureConnectedReceived()

    wsClient.send(
      s"""["SUBSCRIBE\nid:sub-0
          |destination:/exchange/seahorse/seahorse.$workflowId.to\n\n\u0000"]""".stripMargin)

    wsClient.send(
      s"""["SUBSCRIBE\nid:sub-1
          |destination:/exchange/seahorse/workflow.$workflowId.$workflowId.to\n\n\u0000"]"""
        .stripMargin)

    code(ctx)

    Await.result(httpClient.url(s"$sessionsUrl/$workflowId").delete(), 10 seconds)
  }

  private def createSessionBody(workflowId: String, clusterDetails: ClusterDetails) = {
    import spray.json._
    import SessionsJsonProtocol._

    val request = CreateSession(
      workflowId = workflowId,
      cluster = clusterDetails
    )
    request.toJson.compactPrint
  }

  private def ensureConnectedReceived()(implicit ctx: ExecutorContext): Unit = {
    withClue(ctx.websocketMessages) {
      eventually {
        ctx.websocketMessages.find { msg =>
          msg.contains("CONNECTED")
        } shouldBe defined
      }
    }
  }

  private def ensureExecutorIsRunning(workflowId: String): Unit = {
    eventually {
      val statusString = Await.result(
        httpClient.url(sessionsUrl)
          .get
          .map(request => {
            val JsArray(jsonSessions: Seq[JsObject]) =
              request.json.asInstanceOf[JsObject].value("sessions")
            val jsonSessionForWorkflow = jsonSessions.find(
              _.value("workflowId").asInstanceOf[JsString].value == workflowId
            ).get
            jsonSessionForWorkflow.value("status").asInstanceOf[JsString].value
          }), httpTimeout)
      SessionStatus.withName(statusString) shouldEqual SessionStatus.Running
    }
  }

  def assertAllNodesCompletedSuccessfully
      (workflowId: String)
      (implicit ctx: ExecutorContext): Unit = {
    val numberOfNodes = calculateNumberOfNodes(workflowId)

    val nodesResult = eventually {
      val failedNodes = ctx.websocketMessages.filter { msg =>
        msg.contains("executionStatus") && msg.contains("FAILED")
      }

      if (failedNodes.nonEmpty) {
        s"Failed nodes: ${failedNodes.toString()} for workflow $workflowId".failure
      } else {
        val completedNodeMessages = ctx.websocketMessages.filter { msg =>
          msg.contains("executionStatus") && !msg.contains("FAILED")
        }

        logger.info(s"${completedNodeMessages.length} nodes completed." +
          s" Need all $numberOfNodes nodes completed. Workflow $workflowId")
        completedNodeMessages.length should be >= numberOfNodes

        "All nodes completed".success
      }
    }(PatienceConfig(timeout = workflowTimeout, interval = 5 seconds), implicitly[Position])

    nodesResult match {
      case Success(_) =>
      case Failure(nodeReport) =>
        fail(s"Some nodes failed for workflow $workflowId. Node report: $nodeReport")
    }
  }

  private def calculateNumberOfNodes(workflowId: String): Int = {
    Await.result(
      httpClient.url(s"$baseUrl/v1/workflows/$workflowId")
        .get()
        .map { response =>
          val jsObject = response.json.asInstanceOf[JsObject]
          val nodes =
            jsObject.value("workflow").asInstanceOf[JsObject].value("nodes").asInstanceOf[JsArray]
          nodes.value.size
        }, httpTimeout)
  }

  object MyWebSocket {
    // Based on 'WebSocket' object from library org.jfarcand" % "wcs"
    // In original code there is bug, where maxMessageSize would be assigned
    // to buffer size twice, instead assigning it to buffer size and frame size.

    val listeners: ListBuffer[WebSocketListener] = ListBuffer[WebSocketListener]()
    var nettyConfig: NettyAsyncHttpProviderConfig = new NettyAsyncHttpProviderConfig
    var config: AsyncHttpClientConfig.Builder = new AsyncHttpClientConfig.Builder
    var asyncHttpClient: AsyncHttpClient = null

    def apply(): WebSocket = {
      val options = new Options()
      options.maxMessageSize = 1000 * 1000 * 5
      apply(options)
    }

    private def apply(o: Options): WebSocket = {
      nettyConfig.setWebSocketMaxBufferSize(o.maxMessageSize)
      nettyConfig.setWebSocketMaxFrameSize(o.maxMessageSize) // Fixes bug from 1.4 library
      config.setRequestTimeout(o.idleTimeout).
        setUserAgent(o.userAgent).setAsyncHttpClientProviderConfig(nettyConfig)

      try {
        config.setFollowRedirect(true)
        asyncHttpClient = new AsyncHttpClient(config.build)
      } catch {
        case t: IllegalStateException =>
          config = new AsyncHttpClientConfig.Builder
      }
      new WebSocket(o, None, false, asyncHttpClient, listeners)
    }

  }

}
