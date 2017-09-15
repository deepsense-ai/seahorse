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
import io.deepsense.commons.utils.{Logging, Version}
import io.deepsense.graph.Node
import io.deepsense.graph.Node.Id
import io.deepsense.models.json.workflow.exceptions.WorkflowVersionNotSupportedException
import io.deepsense.models.workflows.Workflow.Id
import io.deepsense.models.workflows._
import io.deepsense.workflowmanager.exceptions.{WorkflowOwnerMismatchException, WorkflowNotFoundException}
import io.deepsense.workflowmanager.model.WorkflowDescription
import io.deepsense.workflowmanager.rest.CurrentBuild
import io.deepsense.workflowmanager.storage._

/**
 * Implementation of Workflow Manager.
 */
class WorkflowManagerImpl @Inject()(
    authorizatorProvider: AuthorizatorProvider,
    workflowStorage: WorkflowStorage,
    workflowStateStorage: WorkflowStateStorage,
    notebookStorage: NotebookStorage,
    @Assisted userContextFuture: Future[UserContext],
    @Named("roles.workflows.get") roleGet: String,
    @Named("roles.workflows.update") roleUpdate: String,
    @Named("roles.workflows.create") roleCreate: String,
    @Named("roles.workflows.delete") roleDelete: String)
    (implicit ec: ExecutionContext)
  extends WorkflowManager with Logging {

  private def authorizator: Authorizator = authorizatorProvider.forContext(userContextFuture)

  def get(id: Id): Future[Option[WorkflowWithResults]] = {
    logger.debug("Get workflow id: {}", id)
    authorizator.withRole(roleGet) { userContext =>
      workflowStorage.get(id).flatMap{
        case Some(w) =>
          val result = withResults(id, w).map {
            withResults =>
              checkAPICompatibility(withResults.metadata.apiVersion)
              Some(withResults)
          }
          logger.info(s"Workflow with id: $id, $result")
          result
        case None =>
          logger.info(s"Workflow with id: $id, not found")
          Future.successful(None)
      }
    }
  }

  def getInfo(id: Id): Future[Option[WorkflowInfo]] = {
    logger.debug("Get workflow info: {}", id)
    authorizator.withRole(roleGet) { userContext =>
      workflowStorage.get(id).map { workflow =>
        val result = workflow.map(w => workflowInfo(id, w))
        logger.info(s"Workflow info for id: $id, $result")
        result
      }
    }
  }

  def download(id: Id): Future[Option[WorkflowWithVariables]] = {
    logger.debug("Download workflow id: {}", id)
    authorizator.withRole(roleGet) { userContext =>
      getWorkflowWithNotebook(id).map(_.map(withVariables(id, _)))
    }
  }

  def update(workflowId: Id, workflow: Workflow): Future[Unit] = {
    logger.debug(s"Update workflow id: $workflowId, workflow: $workflow")
    authorizator.withRole(roleUpdate) { userContext =>
      workflowStorage.get(workflowId).flatMap {
        case Some(w) =>
          Future.successful {
            checkAPICompatibility(workflow.metadata.apiVersion)
            checkOwner(workflowId, w.ownerId, userContext.user.id)
          }.flatMap {
            _ => workflowStorage.update(workflowId, workflow).map(_ => ())
          }
        case None => throw new WorkflowNotFoundException(workflowId)
      }
    }
  }

  def create(workflow: Workflow): Future[Workflow.Id] = {
    logger.debug("Create workflow: {}", workflow)
    authorizator.withRole(roleCreate) {
      userContext => {
        val workflowId = Workflow.Id.randomId
        val notebooks = extractNotebooks(workflow)
        val workflowWithoutNotebook = workflowWithRemovedNotebooks(workflow)
        val ownerId = userContext.user.id
        val ownerName = userContext.user.name
        Future.successful(checkAPICompatibility(workflow.metadata.apiVersion)).flatMap(_ =>
          workflowStorage.create(workflowId, workflowWithoutNotebook, ownerId, ownerName)
            .flatMap(_ =>
              Future.sequence(notebooks.map {
                case (nodeId, notebookJson) =>
                  notebookStorage.save(workflowId, nodeId, notebookJson.toString)
              }).map(_ => workflowId)
            )
        )
      }
    }
  }

  def delete(id: Id): Future[Boolean] = {
    logger.debug("Delete workflow id: {}", id)
    authorizator.withRole(roleDelete) { userContext =>
      workflowStorage.get(id).flatMap {
        case Some(w) =>
          Future.successful(checkOwner(id, w.ownerId, userContext.user.id)).flatMap {
            _ => workflowStorage.delete(id).map(_ => true)
          }
        case None => Future.successful(false)
      }
    }
  }

  def clone(
      id: Id,
      workflowDescription: WorkflowDescription): Future[Option[WorkflowWithVariables]] = {
    logger.debug("Clone workflow id: {}", id)
    authorizator.withRole(roleCreate) {
      userContext => {
        getWorkflowWithNotebook(id).flatMap {
          case Some(workflow) =>
            val gui = workflow.additionalData.fields("gui").asJsObject
            val guiUpdated = JsObject(
              gui.fields
                .updated("name", JsString(workflowDescription.name))
                .updated("description", JsString(workflowDescription.description))
            )
            val updatedWorkflow = workflow.copy(additionalData = JsObject(
              workflow.additionalData.fields.updated("gui", guiUpdated)))
            create(updatedWorkflow).map(id => Some(withVariables(id, updatedWorkflow)))
          case None => Future.successful(None)
        }
      }
    }
  }

  def list(): Future[Seq[WorkflowInfo]] = {
    logger.debug("List workflows")
    authorizator.withRole(roleGet) { userContext =>
      workflowStorage.getAll().map { workflows =>
        workflows.map {
          case (workflowId, fullInfo) =>
            workflowInfo(workflowId, fullInfo)
        }.toSeq
      }
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

  override def copyNotebook(
      workflowId: Id,
      sourceNodeId: Id,
      destinationNodeId: Id): Future[Unit] = {
    authorizator.withRole(roleUpdate) { _ =>
      notebookStorage.get(workflowId, sourceNodeId).map(notebookOpt =>
        notebookOpt.foreach(
          notebook => notebookStorage.save(workflowId, destinationNodeId, notebook)
        )
      )
    }
  }

  override def updateStates(
      workflowId: Workflow.Id,
      executionReport: ExecutionReport): Future[Unit] = {
    authorizator.withRole(roleUpdate) { _ =>
      workflowStateStorage.save(workflowId, executionReport.states)
    }
  }

  override def updateStructAndStates(
      workflowId: Workflow.Id,
      workflowWithResults: WorkflowWithResults): Future[Unit] = {
    authorizator.withRole(roleUpdate) { _ =>
      val workflow = Workflow(
        workflowWithResults.metadata,
        workflowWithResults.graph,
        workflowWithResults.thirdPartyData)
      update(workflowId, workflow).flatMap( _ =>
        workflowStateStorage.save(workflowId, workflowWithResults.executionReport.states))
    }
  }

  private def workflowInfo(
      workflowId: Workflow.Id,
      thirdPartyData: JsObject,
      created: DateTime,
      updated: DateTime,
      ownerId: String,
      ownerName: String): WorkflowInfo = {

    def getOptionalString(jsObject: JsObject, field: String) = {
      jsObject.fields.get(field).map(_.asInstanceOf[JsString].value).getOrElse("")
    }

    val gui = thirdPartyData.fields.get("gui").map(_.asJsObject).getOrElse(JsObject())
    WorkflowInfo(workflowId,
      getOptionalString(gui, "name"), getOptionalString(gui, "description"),
      created, updated, ownerId, ownerName
    )
  }

  private def workflowInfo(id: Workflow.Id, fullInfo: WorkflowFullInfo): WorkflowInfo = {
    workflowInfo(
      workflowId = id,
      thirdPartyData = fullInfo.workflow.additionalData,
      created = fullInfo.created,
      updated = fullInfo.updated,
      ownerId = fullInfo.ownerId,
      ownerName = fullInfo.ownerName)
  }

  private def withResults(
      id: Workflow.Id,
      workflowFullInfo: WorkflowFullInfo): Future[WorkflowWithResults] = {
    val workflow = workflowFullInfo.workflow
    val info = workflowInfo(id, workflow.additionalData,
      workflowFullInfo.created, workflowFullInfo.updated,
      workflowFullInfo.ownerId, workflowFullInfo.ownerName)
    getExecutionReport(id, workflow).map(
      WorkflowWithResults(id, workflow.metadata, workflow.graph, workflow.additionalData, _, info)
    )
  }

  private def getExecutionReport(
      workflowId: Workflow.Id,
      workflow: Workflow): Future[ExecutionReport] = {
    workflowStateStorage.get(workflowId).map { case allStates =>
      val nodesIds: Set[Node.Id] = workflow.graph.nodes.map(_.id)
      val currentStates = allStates.filterKeys(nodesIds.contains)
      ExecutionReport(currentStates)
    }
  }

  private def withVariables(id: Workflow.Id, workflow: Workflow): WorkflowWithVariables = {
    WorkflowWithVariables(
      id, workflow.metadata, workflow.graph, workflow.additionalData, Variables())
  }

  private def getWorkflowWithNotebook(id: Workflow.Id): Future[Option[Workflow]] = {
    workflowStorage.get(id).flatMap {
      case Some(w) => objectWorkflowWithNotebooks(id, w.workflow)
      case None => Future.successful(None)
    }
  }

  private def objectWorkflowWithNotebooks(id: Workflow.Id, workflow: Workflow)
      : Future[Option[Workflow]] = {
    notebookStorage.getAll(id).map { notebooks =>
      val additionalDataJson = workflow.additionalData
      val enrichedAdditionalDataJson = JsObject(
        additionalDataJson.fields.updated("notebooks",
          JsObject(notebooks.collect {
            case (nodeId, notebook) if workflow.graph.nodes.find(_.id == nodeId).isDefined =>
              (nodeId.toString, notebook.parseJson)
          })))
      Some(Workflow(
        workflow.metadata,
        workflow.graph,
        enrichedAdditionalDataJson))
    }
  }

  private def extractNotebooks(workflow: Workflow): Map[Node.Id, String] =
    workflow.additionalData.fields.get("notebooks").map {
      _.asJsObject.fields.map {
          case (nodeId, notebook) => (Node.Id.fromString(nodeId), notebook.toString)
        }
    }.getOrElse(Map.empty)

  private def workflowWithRemovedNotebooks(workflow: Workflow): Workflow = {
    val thirdPartyDataJson = workflow.additionalData
    val prunedThirdPartyData = JsObject(thirdPartyDataJson.fields - "notebooks")
    Workflow(workflow.metadata, workflow.graph, prunedThirdPartyData)
  }

  private def checkOwner(workflowId: Workflow.Id, owner: String, user: String): Unit = {
    if (owner != user) {
      throw new WorkflowOwnerMismatchException(workflowId)
    }
  }

  private def checkAPICompatibility(workflowAPIVersion: String): Unit = {
    val parsedWorkflowAPIVersion = Version(workflowAPIVersion)
    if (!CurrentBuild.version.compatibleWith(parsedWorkflowAPIVersion)) {
      throw new WorkflowVersionNotSupportedException(parsedWorkflowAPIVersion, CurrentBuild.version)
    }
  }
}
