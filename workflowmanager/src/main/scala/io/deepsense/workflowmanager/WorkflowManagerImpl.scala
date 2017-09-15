/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.workflowmanager

import scala.concurrent.{ExecutionContext, Future}

import com.google.inject.Inject
import com.google.inject.assistedinject.Assisted
import com.google.inject.name.Named

import io.deepsense.commons.auth.usercontext.UserContext
import io.deepsense.commons.auth.{Authorizator, AuthorizatorProvider}
import io.deepsense.commons.models.Id
import io.deepsense.commons.utils.Logging
import io.deepsense.deeplang.inference.InferContext
import io.deepsense.graph.CyclicGraphException
import io.deepsense.models.workflows._
import io.deepsense.workflowmanager.exceptions.WorkflowNotFoundException
import io.deepsense.workflowmanager.storage.WorkflowStorage

/**
 * Implementation of Workflow Manager.
 */
class WorkflowManagerImpl @Inject()(
    authorizatorProvider: AuthorizatorProvider,
    storage: WorkflowStorage,
    inferContext: InferContext,
    @Assisted userContextFuture: Future[UserContext],
    @Named("roles.workflows.get") roleGet: String,
    @Named("roles.workflows.update") roleUpdate: String,
    @Named("roles.workflows.create") roleCreate: String,
    @Named("roles.workflows.delete") roleDelete: String)
    (implicit ec: ExecutionContext)
  extends WorkflowManager with Logging {

  private def authorizator: Authorizator = authorizatorProvider.forContext(userContextFuture)

  def get(id: Id): Future[Option[WorkflowWithKnowledge]] = {
    logger.debug("Get workflow id: {}", id)
    authorizator.withRole(roleGet) { userContext =>
      storage.get(id).map(_.map {
        workflow => withKnowledge(id, workflow)
      })
    }
  }

  def update(workflowId: Id, workflow: Workflow): Future[WorkflowWithKnowledge] = {
    logger.debug(s"Update workflow id: $workflowId, workflow: $workflow")
    if (workflow.graph.containsCycle) {
      Future.failed(new CyclicGraphException())
    } else {
      authorizator.withRole(roleUpdate) { userContext =>
        storage.get(workflowId).flatMap {
          case Some(_) =>
            storage.save(workflowId, workflow).map(_ => withKnowledge(workflowId, workflow))
          case None => throw new WorkflowNotFoundException(workflowId)
        }
      }
    }
  }

  def create(workflow: Workflow): Future[WorkflowWithKnowledge] = {
    logger.debug("Create workflow: {}", workflow)
    if (workflow.graph.containsCycle) {
      Future.failed(new CyclicGraphException())
    } else {
      authorizator.withRole(roleCreate) {
        userContext => {
          val workflowId = Workflow.Id.randomId
          storage.save(workflowId, workflow).map(_ => withKnowledge(workflowId, workflow))
        }
      }
    }
  }

  def delete(id: Id): Future[Boolean] = {
    logger.debug("Delete workflow id: {}", id)
    authorizator.withRole(roleDelete) { userContext =>
      storage.get(id).flatMap {
        case Some(workflow) =>
          storage.delete(id).map(_ => true)
        case None => Future.successful(false)
      }
    }
  }

  private def withKnowledge(id: Workflow.Id, workflow: Workflow): WorkflowWithKnowledge = {
    val knowledge = workflow.graph.inferKnowledge(inferContext)
    WorkflowWithKnowledge(id, workflow.metadata, workflow.graph, workflow.additionalData, knowledge)
  }
}
