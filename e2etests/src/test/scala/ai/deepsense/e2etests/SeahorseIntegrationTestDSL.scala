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

package ai.deepsense.e2etests

import java.io.File
import java.net.URL
import java.util.UUID

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.language.postfixOps
import scalaz.Scalaz._
import scalaz._

import akka.actor.ActorSystem
import akka.util.Timeout
import org.scalactic.source.Position
import org.scalatest.Matchers
import org.scalatest.concurrent.Eventually
import org.scalatest.time.{Seconds, Span}
import spray.http.HttpResponse

import ai.deepsense.api.datasourcemanager.model.DatasourceParams
import ai.deepsense.commons.models.ClusterDetails
import ai.deepsense.commons.utils.Logging
import ai.deepsense.commons.utils.OptionOpts._
import ai.deepsense.deeplang.CatalogRecorder
import ai.deepsense.graph.nodestate.name.NodeStatusName
import ai.deepsense.models.json.graph.GraphJsonProtocol.GraphReader
import ai.deepsense.models.json.workflow.WorkflowWithVariablesJsonProtocol
import ai.deepsense.models.workflows.{Workflow, WorkflowInfo}
import ai.deepsense.sessionmanager.rest.client.SessionManagerClient
import ai.deepsense.sessionmanager.service.Status
import ai.deepsense.workflowmanager.client.WorkflowManagerClient
import ai.deepsense.workflowmanager.versionconverter.DatasourcesClient

trait SeahorseIntegrationTestDSL
    extends Matchers
    with Eventually
    with Logging
    with WorkflowWithVariablesJsonProtocol {

  protected val dockerComposePath = "../deployment/docker-compose/"  // TODO insert jars to managed resources

  private val localJarsDir = new File(dockerComposePath, "jars")
  private val localJarPaths = getJarsFrom(localJarsDir)
  protected val jarsInDockerPaths = localJarPaths
    .map(f => new File("/resources/jars", f.getName))
    .map(_.toURI.toURL)

  private def catalogRecorder: CatalogRecorder =
    CatalogRecorder.fromJars(localJarPaths.map(_.toURI.toURL))
  private def catalogs = catalogRecorder.catalogs
  final def operablesCatalog = catalogs.operables
  final def operationsCatalog = catalogs.operations
  override def graphReader = new GraphReader(operationsCatalog)

  import scala.concurrent.ExecutionContext.Implicits.global

  protected val httpTimeout = 10 seconds
  protected val workflowTimeout = 30 minutes

  protected val baseUrl = new URL("http", "localhost", 33321, "")
  protected val workflowsUrl = new URL(baseUrl, "/v1/workflows/")
  protected val sessionsUrl = new URL(baseUrl, "/v1/sessions/")
  protected val datasourcesUrl = new URL(baseUrl, "/datasourcemanager/v1/")

  private val userId = UUID.fromString("dd63e120-548f-4ac9-8fd5-092bad7616ab")
  private val userName = "Seahorse test"

  implicit val as: ActorSystem = ActorSystem()
  implicit val timeout: Timeout = httpTimeout

  implicit val patience = PatienceConfig(
    timeout = Span(120, Seconds),
    interval = Span(10, Seconds)
  )

  val wmclient = new WorkflowManagerClient(
    workflowsUrl,
    userId,
    userName,
    None
  )

  val smclient = new SessionManagerClient(
    sessionsUrl,
    userId,
    userName,
    None
  )

  val dsclient = new DatasourcesClient(
    datasourcesUrl,
    userId,
    userName
  )

  def ensureSeahorseIsRunning(): Unit = {
    eventually {
      logger.info("Waiting for Seahorse to boot up...")
      Await.result(smclient.fetchSessions(), httpTimeout)
      Await.result(wmclient.fetchWorkflows(), httpTimeout)
      // If wm is running, DatasourceManager is also running.
    }
  }

  protected def uploadWorkflow(fileContents: String): Future[WorkflowInfo] = {
    for {
      id <- wmclient.uploadWorkflow(fileContents)
      workflows <- wmclient.fetchWorkflows()
      workflow <- workflows.find(_.id == id).asFuture
    } yield {
      workflow
    }
  }

  protected def runAndCleanupWorkflow(workflow: WorkflowInfo, cluster: ClusterDetails): Future[Unit] = {
    for {
      _ <- launchWorkflow(cluster, workflow)
      validation = assertAllNodesCompletedSuccessfully(workflow)
      _ <- cleanSession(workflow)
    } yield {
      validation match {
        case Success(_) =>
        case Failure(nodeReport) =>
          fail(s"Some nodes failed for workflow id: ${workflow.id}." +
            s"name: ${workflow.name}'. Node report: ${nodeReport.msg}. Cluster failed: ${cluster.name}. " +
            s"Details in log containing ${cluster.name} string.")
      }
    }
  }

  private def cleanSession(workflow: WorkflowInfo): Future[Unit] = {
    val id = workflow.id
    for {
      _ <- smclient.deleteSession(id)
      _ <- wmclient.deleteWorkflow(id)
    } yield ()
  }

  private def launchWorkflow(cluster: ClusterDetails, workflow: WorkflowInfo): Future[HttpResponse] = {
    val id = workflow.id
    createSessionSynchronously(id, cluster)
    smclient.launchSession(id)
  }

  protected def createSessionSynchronously(id: Workflow.Id, clusterDetails: ClusterDetails): Unit = {
    smclient.createSession(id, clusterDetails)

    eventually {
      Await.result(
        smclient.fetchSession(id).map { s =>
          s.status shouldBe Status.Running
        }, httpTimeout)
    }
  }

  case class NodesFailed(workflowId: Workflow.Id, workflowName: String, failedNodesCount: Int) {
    val msg = s"Errors: $failedNodesCount nodes failed for workflow id: $workflowId. name: $workflowName"
  }
  case object AllNodesCompleted

  private def assertAllNodesCompletedSuccessfully(
      workflow: WorkflowInfo
    ): Validation[NodesFailed, AllNodesCompleted.type] = {
    val numberOfNodesFut = calculateNumberOfNodes(workflow.id)

    val nodesResult = eventually {
      val nodesStatuses = smclient.queryNodeStatuses(workflow.id)
      Await.result(
        for {
          nsr <- nodesStatuses
          errorNodeStatuses = nsr.nodeStatuses.getOrElse(Map.empty).getOrElse(NodeStatusName.Failed, 0)
          completedNodes = nsr.nodeStatuses.getOrElse(Map.empty).getOrElse(NodeStatusName.Completed, 0)
          numberOfNodes <- numberOfNodesFut
        } yield {
          checkCompletedNodesNumber(
            errorNodeStatuses,
            completedNodes,
            numberOfNodes,
            workflow.id,
            workflow.name
          )
        }, httpTimeout)
    }(PatienceConfig(timeout = workflowTimeout, interval = 5 seconds), implicitly[Position])

    nodesResult
  }

  protected def checkCompletedNodesNumber(
      failedNodesCount: Int,
      completedNodes: Int,
      numberOfNodes: Int,
      workflowID: Workflow.Id,
      workflowName: String) = {
    if (failedNodesCount > 0) {
      NodesFailed(workflowID, workflowName, failedNodesCount).failure
    } else {
      logger.info(s"$completedNodes nodes completed." +
        s" Need all $numberOfNodes nodes completed for workflow id: $workflowID. name: $workflowName")

      if (completedNodes > numberOfNodes) {
        logger.error(
          s"""FATAL. INVESTIGATE
              |
              |Number of completed nodes is larger than number of nodes
            """.
            stripMargin)
      }

      completedNodes shouldEqual numberOfNodes

      AllNodesCompleted.success
    }
  }

  private def calculateNumberOfNodes(workflowId: Workflow.Id): Future[Int] = {
    wmclient.fetchWorkflow(workflowId).map(_.graph.nodes.size)
  }

  protected def insertDatasource(uuid: UUID, datasourceParams: DatasourceParams): Unit = {
    dsclient.insertDatasource(uuid, datasourceParams)
  }

  private def getJarsFrom(dir: File): Seq[File] = {
    Option(dir.listFiles).getOrElse(Array()).filter(f => f.isFile && f.getName.endsWith(".jar"))
  }
}
