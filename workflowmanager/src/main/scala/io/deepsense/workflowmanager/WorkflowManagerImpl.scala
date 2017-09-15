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
 * Implementation of Experiment Manager
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
    @Named("RunningExperimentsActor") runningExperimentsActor: ActorRef,
    @Named("runningexperiments.timeout") timeoutMillis: Long)
    (implicit ec: ExecutionContext)
  extends WorkflowManager with Logging {

  implicit val runningExperimentsTimeout = Timeout(timeoutMillis, TimeUnit.MILLISECONDS)

  private def authorizator: Authorizator = authorizatorProvider.forContext(userContextFuture)

  def get(id: Id): Future[Option[Workflow]] = {
    logger.debug("Get experiment id: {}", id)
    authorizator.withRole(roleGet) { userContext =>
      val experiment = storage.get(userContext.tenantId, id).flatMap {
        case Some(storedExperiment) =>
          val ownedExperiment = storedExperiment.assureOwnedBy(userContext)
          runningExperiment(id).map {
            case running: Some[Workflow] => running
            case None => Some(ownedExperiment)
          }
        case None => Future.successful(None)
      }
      experiment
    }
  }

  def update(experimentId: Id, experiment: InputWorkflow): Future[Workflow] = {
    logger.debug("Update experiment id: {}, experiment: {}", experimentId, experiment)
    if (experiment.graph.containsCycle) {
      Future.failed(new CyclicGraphException())
    } else {
      val now = DateTimeConverter.now
      authorizator.withRole(roleUpdate) { userContext =>
        val oldExperimentOption = storage.get(userContext.tenantId, experimentId)
        oldExperimentOption.flatMap {
          case Some(oldExperiment) =>
            runningExperiment(experimentId).flatMap {
              case Some(runningExperiment)
                if runningExperiment.state.status == Workflow.Status.Running =>
                throw new WorkflowRunningException(experimentId)
              case _ =>
                runningExperimentsActor ! Delete(experimentId)
                val updatedExperiment = oldExperiment
                  .assureOwnedBy(userContext)
                  .updatedWith(experiment, now)
                storage.save(updatedExperiment).map(_ => updatedExperiment)
            }
          case None => throw new WorkflowNotFoundException(experimentId)
        }
      }
    }
  }

  def create(inputExperiment: InputWorkflow): Future[Workflow] = {
    logger.debug("Create experiment inputExperiment: {}", inputExperiment)
    if (inputExperiment.graph.containsCycle) {
      Future.failed(new CyclicGraphException())
    } else {
      val now = DateTimeConverter.now
      authorizator.withRole(roleCreate) {
        userContext => {
          val experiment = inputExperiment.toWorkflowOf(userContext, now)
          storage.save(experiment).map(_ => experiment)
        }
      }
    }
  }

  def experiments(
      limit: Option[Int],
      page: Option[Int],
      status: Option[Workflow.Status.Value]): Future[WorkflowsList] = {
    logger.debug("List experiments limit: {}, page: {}, status: {}", limit, page, status)
    authorizator.withRole(roleList) { userContext =>
      val tenantExperimentsFuture: Future[Seq[Workflow]] =
        storage.list(userContext.tenantId, limit, page, status)
      val runningExperimentsFuture: Future[Map[Id, Workflow]] = runningExperimentsActor
        .ask(GetAllByTenantId(userContext.tenantId))
        .mapTo[WorkflowsMap]
        .map(_.experimentsByTenantId.getOrElse(userContext.tenantId, Set())
          .map(experiment => experiment.id -> experiment).toMap)

      val runningAndStoredExperiments = for {
        tenantExperiments <- tenantExperimentsFuture
        runningExperiments <- runningExperimentsFuture
      } yield tenantExperiments
        .map(experiment => runningExperiments.getOrElse(experiment.id, experiment))

      runningAndStoredExperiments.map(rse => {
        WorkflowsList(Count(rse.size, rse.size), rse)
      })
    }
  }

  def delete(id: Id): Future[Boolean] = {
    logger.debug("Delete experiment id: {}", id)
    authorizator.withRole(roleDelete) { userContext =>
      storage.get(userContext.tenantId, id).flatMap {
        case Some(experiment) =>
          experiment.assureOwnedBy(userContext)
          storage.delete(userContext.tenantId, id).map(_ => true)
        case None => Future.successful(false)
      }
    }
  }

  def launch(
      id: Id,
      targetNodes: Seq[Node.Id]): Future[Workflow] = {
    logger.debug("Launch experiment id: {}, targetNodes: {}", id, targetNodes)
    authorizator.withRole(roleLaunch) { userContext =>
      storage.get(userContext.tenantId, id).flatMap {
        case Some(experiment) =>
          val ownedExperiment = experiment.assureOwnedBy(userContext)
          val launchedExp = runningExperimentsActor.ask(Launch(ownedExperiment))
            .mapTo[Try[Workflow]]
          launchedExp.map {
            case Success(e) => e
            case Failure(e) => throw new WorkflowRunningException(experiment.id)
          }
        case None => throw new WorkflowNotFoundException(id)
      }
    }
  }

  def abort(id: Id, nodes: Seq[Node.Id]): Future[Workflow] = {
    logger.debug("Abort experiment id: {}, targetNodes: {}", id, nodes)
    authorizator.withRole(roleAbort) { userContext =>
      val experimentFuture = storage.get(userContext.tenantId, id)
      experimentFuture.flatMap {
        case Some(experiment) =>
          val ownedExperiment = experiment.assureOwnedBy(userContext)
          runningExperimentsActor
            .ask(Abort(ownedExperiment.id))
            .mapTo[Try[Workflow]]
            .map { _.recover { case e: WorkflowNotRunningException =>
              experiment
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

  private def runningExperiment(id: Id): Future[Option[Workflow]] = {
    runningExperimentsActor
      .ask(Get(id))
      .mapTo[Option[Workflow]]
  }
}
