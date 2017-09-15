/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Grzegorz Chilkiewicz
 */
package io.deepsense.experimentmanager.execution

import javax.inject.{Inject, Named}

import scala.collection.mutable
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{Future, duration}

import akka.actor.{Actor, ActorLogging}
import akka.pattern.pipe
import akka.util.Timeout

import io.deepsense.experimentmanager.execution.RunningExperimentsActor._
import io.deepsense.experimentmanager.models.Experiment
import io.deepsense.experimentmanager.models.Experiment.Id
import io.deepsense.graphexecutor.{Constants, GraphExecutorClient}

class RunningExperimentsActor @Inject() (
    @Named("runningexperiments.timeout") timeoutMillis: Long,
    @Named("runningexperiments.refresh.duration") statusRefreshMillis: Long,
    graphExecutorFactory: GraphExecutorClientFactory)
  extends Actor with ActorLogging {

  val refreshDelay = new FiniteDuration(statusRefreshMillis, duration.MILLISECONDS)
  val experiments = mutable.Map[Experiment.Id, (Experiment, GraphExecutorClient)]()

  implicit val timeout: Timeout = Timeout(timeoutMillis, duration.MILLISECONDS)
  import InternalMessages._
  import context.dispatcher

  context.system.scheduler.schedule(refreshDelay, refreshDelay, self, Tick())

  override def receive: Receive = {
    case internal: InternalMessage => processInternal(internal)
    case external: Message => processProtocol(external)
    case x => unhandled(x)
  }

  private def processInternal(message: InternalMessage): Unit = message match {
    case Tick() => refreshStatuses()
    case ExperimentStatusUpdated(experiment) =>
      statusUpdated(experiment)
  }

  private def statusUpdated(experiment: Experiment): Unit = {
    val oldStatus = experiments(experiment.id)
    experiments.put(experiment.id, (experiment, oldStatus._2))
  }

  private def processProtocol(message: Message): Unit = message match {
    case Launch(experiment) => launch(experiment)
    case Abort(experimentId) => abort(experimentId)
    case GetStatus(experimentId) => requestStatus(experimentId)
    case ListExperiments(tenantId) => listExperiments(tenantId)
    case e: Experiments => unhandled(e)
    case s: Status => unhandled(s)
  }

  private def launch(experiment: Experiment): Unit = {
    log.info(s"RunningExperimentsActor starts launching experiment: $experiment")
    val gec = graphExecutorFactory.create()
    val resultExp = Experiment(
      experiment.id,
      experiment.tenantId,
      experiment.name,
      experiment.graph.copy(nodes = experiment.graph.nodes.map(_.markQueued)),
      experiment.description)
    experiments.put(resultExp.id, (resultExp, gec))
    sender() ! Status(Some(resultExp))
    Future({
      gec.spawnOnCluster()
      gec.waitForSpawn(Constants.WaitForGraphExecutorClientInitDelay)
      gec.sendGraph(resultExp.graph)
    })
  }

  private def abort(id: Id): Unit = {
    log.info(s"RunningExperimentsActor starts aborting experiment: $id")
    experiments.get(id) match {
      case None => sender() ! Status(None)
      case Some((experiment, client))  =>
        val aborted = experiment.copy(graph = experiment.graph.abortNodes)
        experiments.put(aborted.id, (aborted, client))
        Future(client.terminateExecution())
        sender() ! Status(Some(aborted))
    }
    log.info(s"RunningExperimentsActor finishes aborting experiment: $id")
  }

  /**
   * NOTE: if graph has been already executed, cached value is returned to sender
   * @param id id of an experiment
   */
  private def requestStatus(id: Id): Unit = {
    log.info(s"RunningExperimentsActor starts getting status of an experiment: $id")
    val experiment = experiments.get(id).map(_._1)
    sender() ! Status(experiment)
  }

  private def getExecutionState(
      experiment: Experiment,
      graphExecutorClient: GraphExecutorClient): Experiment = {
    experiment.copy(graph = graphExecutorClient.getExecutionState())
  }

  private def listExperiments(tenantId: Option[String]): Unit = {
    log.info(s"RunningExperimentsActor starts listing experiments $tenantId")
    val experimentsByTenant = experiments.map(_._2._1)
      .groupBy(_.tenantId).mapValues(_.toSet)
    log.info(s"RunningExperimentsActor finishes listing experiments $tenantId")
    tenantId match {
      case Some(tenant) =>
        sender() ! Experiments(experimentsByTenant.filter(p => p._1 == tenant))
      case None => sender() ! Experiments(experimentsByTenant)
    }
  }

  private def refreshStatuses(): Unit = {
    experiments.values.foreach { case (experiment, client) =>
      Future(getExecutionState(experiment, client))
        .map(experiment => ExperimentStatusUpdated(experiment))
        .pipeTo(self)
    }
  }

  private object InternalMessages {
    sealed abstract class InternalMessage
    case class Tick() extends InternalMessage
    case class ExperimentStatusUpdated(experiment: Experiment) extends InternalMessage
  }
}

object RunningExperimentsActor {
  sealed abstract class Message
  case class Launch(experiment: Experiment) extends Message
  case class Abort(experimentId: Id) extends Message
  case class GetStatus(experimentId: Id) extends Message
  case class ListExperiments(tenantId: Option[String]) extends Message
  case class Status(experiment: Option[Experiment]) extends Message
  case class Experiments(experimentsByTenantId: Map[String, Set[Experiment]]) extends Message
}
