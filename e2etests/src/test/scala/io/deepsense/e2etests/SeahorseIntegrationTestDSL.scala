/**
  * Copyright (c) 2016, CodiLime Inc.
  */

package io.deepsense.e2etests

import java.net.URL

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.language.postfixOps
import scalaz.Scalaz._
import scalaz._

import akka.actor.ActorSystem
import akka.util.Timeout
import com.ning.http.client.providers.netty.NettyAsyncHttpProviderConfig
import com.ning.http.client.ws.WebSocketListener
import com.ning.http.client.{AsyncHttpClient, AsyncHttpClientConfig, Response => AHCResponse}
import org.jfarcand.wcs.{Options, WebSocket}
import org.scalactic.source.Position
import org.scalatest.{Assertion, Matchers, Succeeded}
import org.scalatest.concurrent.Eventually
import org.scalatest.time.{Seconds, Span}
import play.api.libs.json.{JsArray, JsObject, JsString, Json}
import play.api.libs.ws.ning.NingWSClient

import io.deepsense.commons.models.ClusterDetails
import io.deepsense.commons.utils.Logging
import io.deepsense.deeplang.CatalogRecorder
import io.deepsense.deeplang.catalogs.doperable.DOperableCatalog
import io.deepsense.deeplang.catalogs.doperations.DOperationsCatalog
import io.deepsense.e2etests.stompoverws.StompListener
import io.deepsense.models.json.graph.GraphJsonProtocol.GraphReader
import io.deepsense.models.json.workflow.WorkflowJsonProtocol
import io.deepsense.models.workflows.{ExecutionReport, Workflow, WorkflowInfo}
import io.deepsense.sessionmanager.rest._
import io.deepsense.sessionmanager.rest.requests._
import io.deepsense.sessionmanager.service.{Status => SessionStatus}
import io.deepsense.workflowmanager.client.WorkflowManagerClient


trait SeahorseIntegrationTestDSL extends Matchers with Eventually with Logging with WorkflowJsonProtocol{

  import scala.concurrent.ExecutionContext.Implicits.global

  protected val httpTimeout = 10 seconds
  protected val workflowTimeout = 30 minutes

  protected val baseUrl = new URL("http", "localhost", 33321, "")
  protected val workflowsUrl = new URL(baseUrl, "/v1/workflows/")
  protected val sessionsUrl = new URL(baseUrl, "/v1/sessions/")

  implicit val as: ActorSystem = ActorSystem()
  implicit val timeout: Timeout = httpTimeout

  val operablesCatalog = new DOperableCatalog
  val operationsCatalog = DOperationsCatalog()

  CatalogRecorder.registerDOperables(operablesCatalog)
  CatalogRecorder.registerDOperations(operationsCatalog)

  override val graphReader = new GraphReader(operationsCatalog)

  val client = new WorkflowManagerClient(
    workflowsUrl
  )

  protected val httpClient = NingWSClient()

  // Values from docker-compose file
  private val mqUser = "yNNp7VJS"
  private val mqPass = "1ElYfGNW"

  private val ws = "ws://localhost:33321/stomp/645/bg1ozddg/websocket"

  private implicit val patience = PatienceConfig(
    timeout = Span(60, Seconds),
    interval = Span(10, Seconds)
  )

  def runWorkflowSynchronously(workflow: WorkflowInfo, workflowId: Workflow.Id): Unit = {
    withExecutor(workflowId, TestClusters.local()) { implicit ctx =>
      launch(workflowId)
      assertAllNodesCompletedSuccessfully(workflow)
    }
  }

  def ensureSeahorseIsRunning(): Unit = {
    eventually {
      logger.info("Waiting for Seahorse to boot up...")
      val smIsUp = Await.result(httpClient.url(sessionsUrl.toString).get, httpTimeout)
      val wmIsUp = Await.result(httpClient.url(workflowsUrl.toString).get, httpTimeout)
    }
  }

  case class ExecutorContext(
    wsClient: WebSocket,
    websocketMessages: mutable.MutableList[String],
    executionReports: mutable.MutableList[ExecutionReport]
  )

  // TODO Extract SessionManagerClient and move SM-related logic in there.
  // TODO Use SM-module classes to achieve type-safety
  def launch(workflowId: Workflow.Id)(implicit ctx: ExecutorContext): Unit = {
    logger.info(s"Launching workflow $workflowId")
    ctx.wsClient.send(
      s"""["SEND\ndestination:/exchange/seahorse/workflow.$workflowId.""" +
        s"""$workflowId.from\n\n{\\"messageType\\":\\"launch\\",\\"messageBody\\"""" +
        s""":{\\"workflowId\\":\\"$workflowId\\",\\"nodesToExecute\\":[]}}\u0000"]"""
          .stripMargin)
  }

  def withExecutor[T](workflowId: Workflow.Id, clusterDetails: ClusterDetails)(code: ExecutorContext => T): Unit = {
    logger.info(s"Starting executor for workflow $workflowId")

    httpClient.url(sessionsUrl.toString).post(Json.parse(createSessionBody(workflowId, clusterDetails)))

    ensureExecutorIsRunning(workflowId)
    logger.info(s"Executor for workflow $workflowId started")

    val stompListener = new StompListener()
    val wsClient = MyWebSocket().open(ws).listener(stompListener)

    implicit val ctx = ExecutorContext(wsClient, stompListener.websocketMessages, stompListener.executionReports)

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

  private def createSessionBody(workflowId: Workflow.Id, clusterDetails: ClusterDetails) = {
    import spray.json._

    import SessionsJsonProtocol._

    val request = CreateSession(
      workflowId = workflowId,
      cluster = clusterDetails
    )
    request.toJson.compactPrint
  }

  private def ensureConnectedReceived()(implicit ctx: ExecutorContext): Unit = {
    withClue(ctx.executionReports) {
      eventually {
        ctx.websocketMessages.find { msg =>
          msg.contains("CONNECTED")
        } shouldBe defined
      }
    }
  }

  private def ensureExecutorIsRunning(workflowId: Workflow.Id): Unit = {
    eventually {
      val statusString = Await.result(
        httpClient.url(sessionsUrl.toString)
          .get
          .map(request => {
            val JsArray(jsonSessions: Seq[_]) =
              request.json.asInstanceOf[JsObject].value("sessions")
            val jsonSessionForWorkflow = jsonSessions.find { case obj: JsObject =>
              obj.value("workflowId").asInstanceOf[JsString].value == workflowId.toString()
            }.get.asInstanceOf[JsObject]
            jsonSessionForWorkflow.value("status").asInstanceOf[JsString].value
          }), httpTimeout)
      SessionStatus.withName(statusString) shouldEqual SessionStatus.Running
    }
  }

  def assertAllNodesCompletedSuccessfully
      (workflow: WorkflowInfo)
      (implicit ctx: ExecutorContext): Unit = {
    val numberOfNodes = calculateNumberOfNodes(workflow.id)

    val nodesResult = eventually {
      val nodesStatuses = ctx.executionReports.flatMap(_.nodesStatuses.values).toSet
      val errorsNodesStatuses = nodesStatuses.filter(_.isFailed)

      if (errorsNodesStatuses.nonEmpty) {
        s"Errors: $errorsNodesStatuses for workflow id: ${workflow.id}. name: ${workflow.name}".failure
      } else {
        val completedNodeMessages = nodesStatuses.filter(_.isCompleted)

        logger.info(s"${completedNodeMessages.length} nodes completed." +
          s" Need all $numberOfNodes nodes completed for workflow id: ${workflow.id}. name: ${workflow.name}")

        if(completedNodeMessages.length > numberOfNodes) {
          logger.error(
            s"""FATAL. INVESTIGATE
              |
              |List of completed node messages is longer than number of nodes!
              |
              |All messages:
              |${ctx.websocketMessages}
            """.stripMargin)
        }

        completedNodeMessages.length shouldEqual numberOfNodes

        "All nodes completed".success
      }
    }(PatienceConfig(timeout = workflowTimeout, interval = 5 seconds), implicitly[Position])

    nodesResult match {
      case Success(_) =>
      case Failure(nodeReport) =>
        fail(s"Some nodes failed for workflow id: ${workflow.id}. name: ${workflow.name}'. Node report: $nodeReport")
    }
  }

  private def calculateNumberOfNodes(workflowId: Workflow.Id): Int = {
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
