/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.workflowmanager

import scala.concurrent.{ExecutionContext, Future}

import com.google.inject.Inject
import com.google.inject.assistedinject.Assisted
import com.google.inject.name.Named
import org.joda.time.DateTime
import spray.json._

import io.deepsense.commons.auth.usercontext.UserContext
import io.deepsense.commons.auth.{Authorizator, AuthorizatorProvider}
import io.deepsense.commons.models.Id
import io.deepsense.commons.utils.Logging
import io.deepsense.deeplang.inference.InferContext
import io.deepsense.graph.{CyclicGraphException, Node}
import io.deepsense.models.workflows._
import io.deepsense.workflowmanager.exceptions.WorkflowNotFoundException
import io.deepsense.workflowmanager.storage.{NotebookStorage, WorkflowResultsStorage, WorkflowStorage}

/**
 * Implementation of Workflow Manager.
 */
class WorkflowManagerImpl @Inject()(
    authorizatorProvider: AuthorizatorProvider,
    workflowStorage: WorkflowStorage,
    workflowResultsStorage: WorkflowResultsStorage,
    notebookStorage: NotebookStorage,
    inferContext: InferContext,
    @Assisted userContextFuture: Future[UserContext],
    @Named("roles.workflows.get") roleGet: String,
    @Named("roles.workflows.update") roleUpdate: String,
    @Named("roles.workflows.create") roleCreate: String,
    @Named("roles.workflows.delete") roleDelete: String)
    (implicit ec: ExecutionContext)
  extends WorkflowManager with Logging {

  private def authorizator: Authorizator = authorizatorProvider.forContext(userContextFuture)

  def get(id: Id): Future[Option[Either[String, WorkflowWithKnowledge]]] = {
    logger.debug("Get workflow id: {}", id)
    authorizator.withRole(roleGet) { userContext =>
      workflowStorage.get(id).map {
        _.map { _.right.map(withKnowledge(id, _)) }
      }
    }
  }

  def download(id: Id): Future[Option[Either[String, WorkflowWithVariables]]] = {
    logger.debug("Download workflow id: {}", id)
    authorizator.withRole(roleGet) { userContext =>
      getWorkflowWithNotebook(id).map {
          _.map { _.right.map(withVariables(id, _))
        }
      }
    }
  }

  def update(workflowId: Id, workflow: Workflow): Future[WorkflowWithKnowledge] = {
    logger.debug(s"Update workflow id: $workflowId, workflow: $workflow")
    whenGraphAcyclic(workflow) {
      authorizator.withRole(roleUpdate) { userContext =>
        workflowStorage.get(workflowId).flatMap {
          case Some(_) =>
            workflowStorage.save(workflowId, workflow).map(_ => withKnowledge(workflowId, workflow))
          case None => throw new WorkflowNotFoundException(workflowId)
        }
      }
    }
  }

  def create(workflow: Workflow): Future[WorkflowWithKnowledge] = {
    logger.debug("Create workflow: {}", workflow)
    whenGraphAcyclic(workflow) {
      authorizator.withRole(roleCreate) {
        userContext => {
          val workflowId = Workflow.Id.randomId
          val notebooks = extractNotebooks(workflow)
          val workflowWithoutNotebook = workflowWithRemovedNotebooks(workflow)
          workflowStorage.save(workflowId, workflowWithoutNotebook).flatMap(_ =>
            Future.sequence(notebooks.map {
              case (nodeId, notebookJson) =>
                notebookStorage.save(workflowId, nodeId, notebookJson.toString)
            }).map(_ => withKnowledge(workflowId, workflow))
          )
        }
      }
    }
  }

  def delete(id: Id): Future[Boolean] = {
    logger.debug("Delete workflow id: {}", id)
    authorizator.withRole(roleDelete) { userContext =>
      workflowStorage.get(id).flatMap {
        case Some(workflow) =>
          workflowStorage.delete(id).map(_ => true)
        case None => Future.successful(false)
      }
    }
  }

  def saveWorkflowResults(
    workflowWithResults: WorkflowWithResults): Future[WorkflowWithSavedResults] = {
    authorizator.withRole(roleCreate) { userContext =>
      val resultsId = ExecutionReportWithId.Id.randomId
      val workflowWithSavedResults =
        WorkflowWithSavedResults(resultsId, workflowWithResults)

      workflowStorage.saveExecutionResults(workflowWithSavedResults)
        .map(_ => workflowResultsStorage.save(workflowWithSavedResults))
        .map(_ => workflowWithSavedResults)
    }
  }

  override def getLatestExecutionReport(
      workflowId: Workflow.Id): Future[Option[Either[String, WorkflowWithSavedResults]]] = {
    authorizator.withRole(roleGet) { userContext =>
      workflowStorage.getLatestExecutionResults(workflowId)
    }
  }


  override def getResultsUploadTime(workflowId: Workflow.Id): Future[Option[DateTime]] = {
    authorizator.withRole(roleGet) { _ =>
      workflowStorage.getResultsUploadTime(workflowId)
    }
  }

  override def getExecutionReport(
      id: ExecutionReportWithId.Id): Future[Option[Either[String, WorkflowWithSavedResults]]] = {
    authorizator.withRole(roleGet) { userContext =>
      workflowResultsStorage.get(id)
    }
  }

  override def getNotebook(workflowId: Workflow.Id, nodeId: Node.Id): Future[Option[String]] = {
    authorizator.withRole(roleGet) { _ =>
      notebookStorage.get(workflowId, nodeId)
    }
  }

  override def saveNotebook(
      workflowId: Workflow.Id,
      nodeId: Node.Id,
      notebook: String): Future[Unit] = {
    authorizator.withRole(roleUpdate) { _ =>
      notebookStorage.save(workflowId, nodeId, notebook)
    }
  }

  private def withKnowledge(id: Workflow.Id, workflow: Workflow): WorkflowWithKnowledge = {
    val knowledge = workflow.graph.inferKnowledge(inferContext)
    WorkflowWithKnowledge(id, workflow.metadata, workflow.graph, workflow.additionalData, knowledge)
  }

  private def withVariables(id: Workflow.Id, workflow: Workflow): WorkflowWithVariables = {
    WorkflowWithVariables(
      id, workflow.metadata, workflow.graph, workflow.additionalData, Variables())
  }

  private def getWorkflowWithNotebook(id: Workflow.Id): Future[Option[Either[String, Workflow]]] = {
    workflowStorage.get(id).flatMap {
      case Some(workflowStorageResult) => workflowStorageResult.fold(
        stringWorkflow => stringWorkflowWithNotebooks(id, stringWorkflow),
        objectWorkflow => objectWorkflowWithNotebooks(id, objectWorkflow))
      case None => Future.successful(None)
    }
  }

  private def stringWorkflowWithNotebooks(
      id: Workflow.Id, stringWorkflow: String): Future[Option[Either[String, Workflow]]] = {
    notebookStorage.getAll(id).map { notebooks =>
      val workflowJson = stringWorkflow.parseJson.asJsObject
      val thirdPartyData = workflowJson.fields("thirdPartyData").asJsObject
      val thirdPartyDataWithNotebooks = JsObject(
        thirdPartyData.fields.updated("notebooks",
          JsObject(notebooks.map {
            case (nodeId, notebook) => (nodeId.toString, notebook.parseJson)
          })))
      val workflowWithNotebooks = JsObject(
        workflowJson.fields.updated("thirdPartyData", thirdPartyDataWithNotebooks))
      Some(Left(workflowWithNotebooks.toString))
    }
  }

  private def objectWorkflowWithNotebooks(id: Workflow.Id, workflow: Workflow)
      : Future[Option[Either[String, Workflow]]] = {
    notebookStorage.getAll(id).map { notebooks =>
      val additionalDataJson = workflow.additionalData.data.parseJson.asJsObject
      val enrichedAdditionalDataJson = JsObject(
        additionalDataJson.fields.updated("notebooks",
          JsObject(notebooks.map {
            case (nodeId, notebook) => (nodeId.toString, notebook.parseJson)
          })))
      Some(Right(Workflow(
        workflow.metadata,
        workflow.graph,
        ThirdPartyData(enrichedAdditionalDataJson.toString))))
    }
  }

  private def extractNotebooks(workflow: Workflow): Map[Node.Id, String] =
    workflow.additionalData.data.parseJson.asJsObject.fields.get("notebooks").map {
      _.asJsObject.fields.map {
          case (nodeId, notebook) => (Node.Id.fromString(nodeId), notebook.toString)
        }
    }.getOrElse(Map.empty)

  private def workflowWithRemovedNotebooks(workflow: Workflow): Workflow = {
    val thirdPartyDataJson = workflow.additionalData.data.parseJson.asJsObject
    val prunedThirdPartyData = ThirdPartyData(
      JsObject(thirdPartyDataJson.fields - "notebooks").toString)
    Workflow(workflow.metadata, workflow.graph, prunedThirdPartyData)
  }

  private def whenGraphAcyclic[T](workflow: Workflow)(f: => Future[T]): Future[T] = {
    workflow.graph.topologicallySorted match {
      case Some(_) => f
      case None => Future.failed(new CyclicGraphException())
    }
  }
}
