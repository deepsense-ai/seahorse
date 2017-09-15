/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.workflowmanager.storage

import scala.concurrent.{ExecutionContext, Future}

import org.joda.time.DateTime
import spray.json._

import io.deepsense.commons.models.Id
import io.deepsense.commons.utils.{Logging, Version}
import io.deepsense.models.json.workflow.WorkflowVersionUtil
import io.deepsense.models.workflows.Workflow
import io.deepsense.workflowmanager.rest.CurrentBuild

/**
 * Abstraction layer to make implementation of Workflow Manager easier.
 */
trait WorkflowStorage extends WorkflowVersionUtil with Logging {

  /**
   * Returns a workflow with the specified id.
   * @param id Id of the workflow.
   * @return Workflow with the id as an object, or None if the workflow does not exist.
   */
  def get(id: Id): Future[Option[WorkflowFullInfo]]

  /**
   * Creates a workflow.
   * @param id Id of the workflow.
   * @param workflow Workflow to be created.
   * @param ownerId Id of the owner.
   * @param ownerName Name of the owner.
   */
  def create(id: Id, workflow: Workflow, ownerId: String, ownerName: String): Future[Unit] = {
    createRaw(id, workflow.toJson, ownerId, ownerName)
  }

  def createRaw(id: Id, workflow: JsValue, ownerId: String, ownerName: String): Future[Unit]

  /**
   * Updates a workflow.
   * @param id Id of the workflow.
   * @param workflow Workflow to be updated.
   */
  def update(id: Id, workflow: Workflow): Future[Unit] = {
    updateRaw(id, workflow.toJson)
  }

  protected def rawWorkflowToFullWorkflow(raw: WorkflowRaw): WorkflowFullInfo = {
    WorkflowFullInfo(
      workflow = raw.workflow.convertTo[Workflow],
      created = raw.created,
      updated = raw.updated,
      ownerId = raw.ownerId,
      ownerName = raw.ownerName
    )
  }

  /**
   * Returns all stored workflows. If the workflow is compatible with the current
   * API version it is returned as an object otherwise as a string.
   * @return Stored workflows as objects or Strings.
   */
  def getAll(implicit ec: ExecutionContext): Future[Map[Id, WorkflowFullInfo]] = {
    for {
      rawWorkflows <- getAllRaw
    } yield {
      rawWorkflows.mapValues(rawWorkflowToFullWorkflow)
    }
  }

  def updateRaw(id: Id, workflow: JsValue): Future[Unit]

  def getAllRaw: Future[Map[Workflow.Id, WorkflowRaw]]

  /**
   * Removes an workflow with the specified id.
   * @param id Id of the workflow to be deleted.
   * @return Future.successful whether the workflow was found or not.
   *         If there were hard failures (e.g. connection error) the returned
   *         future will fail.
   */
  def delete(id: Id): Future[Unit]

  override def currentVersion: Version = CurrentBuild.version
}

case class WorkflowFullInfo(
    workflow: Workflow,
    created: DateTime,
    updated: DateTime,
    ownerId: String,
    ownerName: String)

case class WorkflowRaw(
    workflow: JsValue,
    created: DateTime,
    updated: DateTime,
    ownerId: String,
    ownerName: String
)
