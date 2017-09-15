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

package ai.deepsense.workflowmanager.storage

import scala.concurrent.{ExecutionContext, Future}

import org.joda.time.DateTime
import spray.json._

import ai.deepsense.commons.models.Id
import ai.deepsense.commons.utils.{Logging, Version}
import ai.deepsense.models.json.workflow.WorkflowVersionUtil
import ai.deepsense.models.workflows.Workflow
import ai.deepsense.workflowmanager.rest.CurrentBuild

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
