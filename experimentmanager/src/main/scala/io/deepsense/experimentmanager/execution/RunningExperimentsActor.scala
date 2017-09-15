/**
 * Copyright (c) 2015, CodiLime, Inc.
 */
package io.deepsense.experimentmanager.execution

import java.util.concurrent.TimeoutException
import javax.inject.{Inject, Named}

import scala.collection.mutable
import scala.concurrent.Future
import scala.concurrent.duration._

import akka.actor.{Actor, ActorLogging}
import akka.pattern.{after, pipe}
import akka.util.Timeout

import io.deepsense.experimentmanager.execution.RunningExperimentsActor._
import io.deepsense.graphexecutor.{Constants, GraphExecutorClient}
import io.deepsense.models.experiments.Experiment
import io.deepsense.models.experiments.Experiment.Id

class RunningExperimentsActor @Inject() (
    @Named("entitystorage.label") entitystorageLabel: String,
    @Named("runningexperiments.timeout") timeoutMillis: Long,
    @Named("runningexperiments.refresh.interval") refreshIntervalMillis: Long,
    @Named("runningexperiments.refresh.timeout") refreshTimeoutMillis: Long,
    graphExecutorFactory: GraphExecutorClientFactory = DefaultGraphExecutorClientFactory())
  extends Actor with ActorLogging {

  val refreshInterval = refreshIntervalMillis.milliseconds
  val refreshTimeout = refreshTimeoutMillis.milliseconds
  val experiments = mutable.Map[Experiment.Id, (Experiment, GraphExecutorClient)]()
  val updateTasks = mutable.Map[Experiment.Id, Future[Any]]()

  implicit val timeout: Timeout = timeoutMillis.milliseconds
  import InternalMessages._
  import context.dispatcher

  context.system.scheduler.schedule(refreshInterval, refreshInterval, self, Tick)

  override def receive: Receive = {
    case internal: InternalMessage => processInternal(internal)
    case external: Message => processProtocol(external)
    case x => unhandled(x)
  }

  private def processInternal(message: InternalMessage): Unit = message match {
    case Tick => refreshStatuses()
    case ExperimentStatusUpdated(experiment) => statusUpdated(experiment)
  }

  private def statusUpdated(experiment: Experiment): Unit = {
    val oldStatus = experiments.get(experiment.id)
    oldStatus.foreach(s => experiments.put(experiment.id, (experiment, s._2)))
  }

  private def processProtocol(message: Message): Unit = message match {
    case Launch(experiment) => launch(experiment)
    case Abort(experimentId) => abort(experimentId)
    case GetStatus(experimentId) => requestStatus(experimentId)
    case ExperimentsByTenant(tenantId) => listExperiments(tenantId)
    case Delete(experimentId) => delete(experimentId)
    case e: ExperimentsMap => unhandled(e)
    case s: Status => unhandled(s)
  }

  private def launch(experiment: Experiment): Unit = {
    log.info(s"RunningExperimentsActor starts launching experiment: $experiment")
    val gec = graphExecutorFactory.create()
    val resultExp = experiment.markRunning
    experiments.put(resultExp.id, (resultExp, gec))
    val s = sender()
    s ! Launched(resultExp)
    Future {
      gec.spawnOnCluster(entitystorageLabel)
      val spawned = gec.waitForSpawn(Constants.WaitForGraphExecutorClientInitDelay)
      if (spawned) {
        gec.sendExperiment(resultExp)
      } else {
        throw new IllegalStateException("Spawning Failed for experiment: " + experiment)
      }
    }.onFailure { case reason =>
        log.error(reason, s"Launching experiment failed $experiment")
        self ! ExperimentStatusUpdated(
          markExperimentAsFailed(resultExp.id, Option(reason.getMessage)))
    }
  }

  private def delete(experimentId: Id): Unit =
    for {
      (exp, _) <- experiments.get(experimentId)
      if exp.state.status != Experiment.Status.Running
    } {
      experiments.remove(exp.id)
      updateTasks.remove(exp.id)
    }

  private def abort(id: Id): Unit = {
    log.info(s"RunningExperimentsActor starts aborting experiment: $id")
    experiments.get(id) match {
      case None => sender() ! Status(None)
      case Some((experiment, client)) =>
        val aborted = experiment.markAborted
        experiments.put(aborted.id, (aborted, client))
        Future(client.terminateExecution()).onFailure {
          case reason => log.error(reason, s"Could not terminate execution of experiment $id")
        }
        sender() ! Status(Some(aborted))
    }
    log.info(s"RunningExperimentsActor finishes aborting experiment: $id")
  }

  /**
   * NOTE: if graph has been already executed, cached value is returned to sender
   * @param id id of an experiment
   */
  private def requestStatus(id: Id): Unit = {
    log.info(s"RunningExperimentsActor requesting status of an experiment: $id")
    val experiment = experiments.get(id).map(_._1)
    sender() ! Status(experiment)
  }

  private def getExecutionState(
      experiment: Experiment,
      graphExecutorClient: GraphExecutorClient): Experiment = {
    graphExecutorClient.getExecutionState()
      .map(experiment.withGraph)
      .getOrElse(experiment)
  }

  private def listExperiments(tenantId: Option[String]): Unit = {
    log.info(s"RunningExperimentsActor listing experiments of tenantId: $tenantId")
    val experimentsByTenant = experiments.map(_._2._1)
      .groupBy(_.tenantId).mapValues(_.toSet)
    log.info(s"RunningExperimentsActor finishes listing experiments $tenantId")
    tenantId match {
      case Some(tenant) =>
        sender() ! ExperimentsMap(experimentsByTenant.filter(p => p._1 == tenant))
      case None => sender() ! ExperimentsMap(experimentsByTenant)
    }
  }

  private def refreshStatuses(): Unit =
    experiments.values.filter {
      case (experiment, client) => experiment.isRunning &&
        updateTasks.get(experiment.id).map(_.isCompleted).getOrElse(true)
    }.foreach { case (experiment, client) =>
      val state = Future { getExecutionState(experiment, client) }
      state.onFailure { case reason =>
        log.error(
          reason,
          "getExecutionState of experiment ${experiment.id} failed! (Is it already started?)")
      }
      val stateWithTimeout = Future firstCompletedOf Seq(state,
        after(refreshTimeout, context.system.scheduler)(Future.failed {
          new TimeoutException(s"getExecutionState of ${experiment.id} has timed out!")
        }))
      updateTasks.put(experiment.id, stateWithTimeout)
      stateWithTimeout.map(experiment => ExperimentStatusUpdated(experiment)).pipeTo(self)
    }

  private object InternalMessages {
    sealed abstract class InternalMessage
    case object Tick extends InternalMessage
    case class ExperimentStatusUpdated(experiment: Experiment) extends InternalMessage
  }

  private def markExperimentAsFailed(
      experimentId: Id,
      failureMessage: Option[String]): Experiment = {
    log.info(s"Marking experiment: $experimentId as failed with message: $failureMessage")
    val message: String = s"Experiment $experimentId failed. " +
      failureMessage.map(m => s"With message: $m").getOrElse("")
    experiments(experimentId)._1.markFailed(message)
  }
}

object RunningExperimentsActor {
  sealed trait Message
  case class Launch(experiment: Experiment) extends Message
  case class Launched(experiment: Experiment) extends Message
  case class Abort(experimentId: Id) extends Message
  case class GetStatus(experimentId: Id) extends Message
  case class ExperimentsByTenant(tenantId: Option[String]) extends Message
  case class Status(experiment: Option[Experiment]) extends Message
  case class ExperimentsMap(experimentsByTenantId: Map[String, Set[Experiment]]) extends Message
  case class Delete(experimentId: Id) extends Message
}
