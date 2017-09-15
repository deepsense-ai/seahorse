/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.workflowmanager.execution

import javax.inject.{Inject, Named}

import scala.collection.mutable
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

import akka.actor._
import akka.util.Timeout

import io.deepsense.graphexecutor.GraphExecutorClientActor
import io.deepsense.graphexecutor.clusterspawner.ClusterSpawner
import io.deepsense.models.messages.{Abort, Delete, Get, GetAllByTenantId, Launch, Update, WorkflowsMap}
import io.deepsense.models.workflows.Workflow
import io.deepsense.models.workflows.Workflow.Id
import io.deepsense.workflowmanager.exceptions.WorkflowNotRunningException

class RunningWorkflowsActor @Inject()(
    @Named("entitystorage.label") val entityStorageLabel: String,
    @Named("runningexperiments.timeout") timeoutMillis: Long,
    val spawner: ClusterSpawner)
  extends Actor with ActorLogging {

  this: GraphExecutorClientFactory =>

  case class WorkflowWithClient(experiment: Workflow, gec: ActorRef)

  private val experiments = mutable.Map[Workflow.Id, WorkflowWithClient]()

  implicit val timeout: Timeout = timeoutMillis.milliseconds

  override def receive: Receive = {
    case Get(eid) => sender ! get(eid)
    case Launch(experiment) => sender ! launch(experiment)
    case Abort(eid) => sender ! abort(eid)
    case GetAllByTenantId(tenantId) => sender ! getAllByTenantId(tenantId)
    case Delete(experimentId) => delete(experimentId)
    case Update(exp) => update(exp)
  }

  def update(experiment: Workflow): Unit = {
    log.info("Experiment status update, experiment.id: {}", experiment.id)
    val gec = experiments(experiment.id).gec
    experiments(experiment.id) = WorkflowWithClient(experiment, gec)
  }

  def launch(experiment: Workflow): Try[Workflow] = {
    log.info(">>> Launch({}) with {} nodes", experiment.id, experiment.graph.nodes.size)
    val isExperimentRunning = experiments.get(experiment.id).map(_.experiment).exists(_.isRunning)
    if (isExperimentRunning) {
      val reason = s"Experiment ${experiment.id} is already running. Rejecting request to launch"
      log.info("Rejected({}, {})", experiment.id, reason)
      Failure(new IllegalStateException(reason))
    } else {
      log.info("Launch accepted. Experiment is not running: {}", experiment.state.status)
      val gec = context.actorOf(
        Props(createGraphExecutorClient()), experiment.id.toString)
      log.info("Created GEC actor: {}", gec)
      experiments.put(experiment.id, WorkflowWithClient(experiment, gec))
      gec ! Launch(experiment)

      // TODO DS-796 REA shouldn't marked experiment as running on launch
      val runningExp = experiment.markRunning
      experiments.put(experiment.id, WorkflowWithClient(runningExp, gec))
      Success(runningExp)
    }
  }

  private def delete(experimentId: Id): Unit = {
    for {
      WorkflowWithClient(exp, _) <- experiments.get(experimentId)
      if exp.state.status != Workflow.Status.Running
    } experiments.remove(exp.id)
  }

  private def abort(id: Id): Try[Workflow] = {
    log.info(">>> {}", Abort(id))
    val result = experiments.get(id) match {
      case None =>
        val error = s"No experiment to abort $id"
        log.error(error)
        Failure(new WorkflowNotRunningException(id))
      case Some(WorkflowWithClient(experiment, client)) if experiment.isRunning =>
        // TODO DS-795 Don't mark experiment as aborted before it's really aborted
        val aborted = experiment.markAborted
        client ! Abort(experiment.id)
        experiments.put(aborted.id, WorkflowWithClient(aborted, client))
        Success(aborted)
      case Some(WorkflowWithClient(experiment, client)) =>
        val error =
          s"Could not terminate experiment $id in state: ${experiment.state.status} (not Running)"
        log.error(error)
        Failure(new WorkflowNotRunningException(id))
    }
    log.info("<<< {} => {}", Abort(id), result)
    result
  }

  def get(eid: Workflow.Id): Option[Workflow] = {
    log.info("Requesting status of an experiment: {}", eid)
    experiments.get(eid).map(_.experiment)
  }

  def getAllByTenantId(tenantId: String): WorkflowsMap = {
    log.info(">>> GetAllByTenantId({})", tenantId)
    val experimentsByTenant = experiments.map(_._2.experiment)
      .groupBy(_.tenantId).mapValues(_.toSet)
    log.info(">>> GetAllByTenantId experiments: {}", tenantId)
    WorkflowsMap(experimentsByTenant.filter(p => p._1 == tenantId))
  }
}

trait GraphExecutorClientFactory {

  def createGraphExecutorClient(): Actor

}

trait ProductionGraphExecutorClientFactory extends GraphExecutorClientFactory {
  val entityStorageLabel: String
  val spawner: ClusterSpawner

  def createGraphExecutorClient(): Actor = new GraphExecutorClientActor(
    entityStorageLabel = entityStorageLabel, spawner = spawner)

}
