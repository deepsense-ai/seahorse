/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.workflowmanager

import java.util.concurrent.TimeUnit

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import com.google.inject.Inject
import com.google.inject.assistedinject.Assisted
import com.google.inject.name.Named

import io.deepsense.commons.auth.usercontext.UserContext
import io.deepsense.commons.auth.{Authorizator, AuthorizatorProvider}
import io.deepsense.commons.datetime.DateTimeConverter
import io.deepsense.commons.models.Id
import io.deepsense.commons.utils.Logging
import io.deepsense.models.actions.{LaunchAction, AbortAction, Action}
import io.deepsense.graph.{CyclicGraphException, Node}
import io.deepsense.models.messages._
import io.deepsense.models.workflows.{Count, InputWorkflow, Workflow, WorkflowsList}
import io.deepsense.workflowmanager.exceptions.{WorkflowNotFoundException, WorkflowNotRunningException, WorkflowRunningException}
import io.deepsense.workflowmanager.storage.WorkflowStorage

/**
 * Implementation of Workflow Manager
 */
class WorkflowManagerImpl @Inject()(
    authorizatorProvider: AuthorizatorProvider,
    storage: WorkflowStorage,
    @Assisted userContextFuture: Future[UserContext],
    @Named("roles.experiments.get") roleGet: String,
    @Named("roles.experiments.update") roleUpdate: String,
    @Named("roles.experiments.create") roleCreate: String,
    @Named("roles.experiments.list") roleList: String,
    @Named("roles.experiments.delete") roleDelete: String,
    @Named("roles.experiments.launch") roleLaunch: String,
    @Named("roles.experiments.abort") roleAbort: String,
    @Named("RunningExperimentsActor") runningWorkflowsActor: ActorRef,
    @Named("runningexperiments.timeout") timeoutMillis: Long)
    (implicit ec: ExecutionContext)
  extends WorkflowManager with Logging {

  implicit val runningExperimentsTimeout = Timeout(timeoutMillis, TimeUnit.MILLISECONDS)

  private def authorizator: Authorizator = authorizatorProvider.forContext(userContextFuture)

  def get(id: Id): Future[Option[Workflow]] = {
    logger.debug("Get workflow id: {}", id)
    authorizator.withRole(roleGet) { userContext =>
      val workflow = storage.get(userContext.tenantId, id).flatMap {
        case Some(storedWorkflow) =>
          val ownedWorkflow = storedWorkflow.assureOwnedBy(userContext)
          runningWorkflow(id).map {
            case running: Some[Workflow] => running
            case None => Some(ownedWorkflow)
          }
        case None => Future.successful(None)
      }
      workflow
    }
  }

  def update(workflowId: Id, workflow: InputWorkflow): Future[Workflow] = {
    logger.debug("Update workflow id: {}, workflow: {}", workflowId, workflow)
    if (workflow.graph.containsCycle) {
      Future.failed(new CyclicGraphException())
    } else {
      val now = DateTimeConverter.now
      authorizator.withRole(roleUpdate) { userContext =>
        val oldWorkflowOption = storage.get(userContext.tenantId, workflowId)
        oldWorkflowOption.flatMap {
          case Some(oldWorkflow) =>
            runningWorkflow(workflowId).flatMap {
              case Some(runningWorkflow)
                if runningWorkflow.state.status == Workflow.Status.Running =>
                throw new WorkflowRunningException(workflowId)
              case _ =>
                runningWorkflowsActor ! Delete(workflowId)
                val updatedWorkflow = oldWorkflow
                  .assureOwnedBy(userContext)
                  .updatedWith(workflow, now)
                storage.save(updatedWorkflow).map(_ => updatedWorkflow)
            }
          case None => throw new WorkflowNotFoundException(workflowId)
        }
      }
    }
  }

  def create(inputWorkflow: InputWorkflow): Future[Workflow] = {
    logger.debug("Create workflow inputWorkflow: {}", inputWorkflow)
    if (inputWorkflow.graph.containsCycle) {
      Future.failed(new CyclicGraphException())
    } else {
      val now = DateTimeConverter.now
      authorizator.withRole(roleCreate) {
        userContext => {
          val workflow = inputWorkflow.toWorkflowOf(userContext, now)
          storage.save(workflow).map(_ => workflow)
        }
      }
    }
  }

  def workflows(
      limit: Option[Int],
      page: Option[Int],
      status: Option[Workflow.Status.Value]): Future[WorkflowsList] = {
    logger.debug("List workflows limit: {}, page: {}, status: {}", limit, page, status)
    authorizator.withRole(roleList) { userContext =>
      val tenantWorkflowsFuture: Future[Seq[Workflow]] =
        storage.list(userContext.tenantId, limit, page, status)
      val runningWorkflowsFuture: Future[Map[Id, Workflow]] = runningWorkflowsActor
        .ask(GetAllByTenantId(userContext.tenantId))
        .mapTo[WorkflowsMap]
        .map(_.workflowsByTenantId.getOrElse(userContext.tenantId, Set())
          .map(workflow => workflow.id -> workflow).toMap)

      val runningAndStoredWorkflow = for {
        tenantWorkflows <- tenantWorkflowsFuture
        runningWorkflow <- runningWorkflowsFuture
      } yield tenantWorkflows
        .map(workflow => runningWorkflow.getOrElse(workflow.id, workflow))

      runningAndStoredWorkflow.map(rse => {
        WorkflowsList(Count(rse.size, rse.size), rse)
      })
    }
  }

  def delete(id: Id): Future[Boolean] = {
    logger.debug("Delete workflow id: {}", id)
    authorizator.withRole(roleDelete) { userContext =>
      storage.get(userContext.tenantId, id).flatMap {
        case Some(workflow) =>
          workflow.assureOwnedBy(userContext)
          storage.delete(userContext.tenantId, id).map(_ => true)
        case None => Future.successful(false)
      }
    }
  }

  def launch(
      id: Id,
      targetNodes: Seq[Node.Id]): Future[Workflow] = {
    logger.debug("Launch workflow id: {}, targetNodes: {}", id, targetNodes)
    authorizator.withRole(roleLaunch) { userContext =>
      storage.get(userContext.tenantId, id).flatMap {
        case Some(workflow) =>
          val ownedWorkflow = workflow.assureOwnedBy(userContext)
          val launchedWorkflow = runningWorkflowsActor.ask(Launch(ownedWorkflow))
            .mapTo[Try[Workflow]]
          launchedWorkflow.map {
            case Success(e) => e
            case Failure(e) => throw new WorkflowRunningException(workflow.id)
          }
        case None => throw new WorkflowNotFoundException(id)
      }
    }
  }

  def abort(id: Id, nodes: Seq[Node.Id]): Future[Workflow] = {
    logger.debug("Abort workflow id: {}, targetNodes: {}", id, nodes)
    authorizator.withRole(roleAbort) { userContext =>
      val workflowFuture = storage.get(userContext.tenantId, id)
      workflowFuture.flatMap {
        case Some(workflow) =>
          val ownedWorkflow = workflow.assureOwnedBy(userContext)
          runningWorkflowsActor
            .ask(Abort(ownedWorkflow.id))
            .mapTo[Try[Workflow]]
            .map { _.recover { case e: WorkflowNotRunningException =>
              workflow
            }.get
          }
        case None => throw new WorkflowNotFoundException(id)
      }
    }
  }

  override def runAction(id: Id, action: Action): Future[Workflow] = action match {
    case AbortAction(nodes) => abort(id, nodes.getOrElse(List()))
    case LaunchAction(nodes) => launch(id, nodes.getOrElse(List()))
  }

  private def runningWorkflow(id: Id): Future[Option[Workflow]] = {
    runningWorkflowsActor
      .ask(Get(id))
      .mapTo[Option[Workflow]]
  }
}
