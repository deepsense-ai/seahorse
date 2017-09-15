package io.deepsense.experimentmanager.execution

import java.util.UUID

import scala.collection.mutable
import scala.concurrent.duration
import scala.concurrent.duration.FiniteDuration
import scala.util.Random

import akka.actor.{Actor, ActorLogging}
import com.google.inject.Inject
import com.google.inject.name.Named

import io.deepsense.experimentmanager.execution.MockRunningExperimentsActor.Tick
import io.deepsense.experimentmanager.execution.RunningExperimentsActor._
import io.deepsense.graph.Node
import io.deepsense.models.experiments.Experiment

class MockRunningExperimentsActor @Inject()(
    @Named("runningexperiments.mock.failureprobability") failureProbability: Double,
    @Named("runningexperiments.mock.tickdelay") tickDelay: Long)
  extends Actor
  with ActorLogging {

  val experiments = mutable.Map[Experiment.Id, Experiment]()
  val random = new Random()
  val tickDuration = new FiniteDuration(tickDelay, duration.MILLISECONDS)

  @throws[Exception](classOf[Exception])
  override def preStart(): Unit = {
    super.preStart()
    import scala.concurrent.ExecutionContext.Implicits.global
    context.system.scheduler.schedule(tickDuration, tickDuration, self, Tick())
  }

  override def receive: Receive = {
    case Launch(experiment) => launch(experiment)
    case Abort(id) => abort(id)
    case GetStatus(id) => getStatus(id)
    case ListExperiments(tenantId) => listExperiments(tenantId)
    case Tick() => updateProgress()
    case x => unhandled(x)
  }

  private def launch(experiment: Experiment): Unit = {
    val enqueuedExperiment = enqueueNodes(experiment)
    experiments += experiment.id -> enqueuedExperiment
    sender() ! Status(Some(enqueuedExperiment))
  }

  private def abort(id: Experiment.Id): Unit = {
    val experiment = experiments.get(id)
      .map(abortNodes).map(experiment => {
      experiments += experiment.id -> experiment
      experiment
    })
    sender() ! Status(experiment)
  }

  private def getStatus(id: Experiment.Id): Unit = {
    sender() ! Status(experiments.get(id))
  }

  private def listExperiments(tenantId: Option[String]): Unit = {
    tenantId match {
      case Some(tenant) =>
        val tenantExperiments =
          experiments.values.groupBy(_.tenantId).getOrElse(tenant, Set()).toSet
        sender() ! Experiments(Map(tenant -> tenantExperiments))
      case None =>
        val experimentsByTenant = experiments.values.groupBy(_.tenantId).mapValues(_.toSet)
        sender () ! Experiments(experimentsByTenant)
    }
  }

  private def updateProgress(): Unit = {
    experiments.foreach { case (id, experiment) =>
      val graph = experiment.graph
      val runningNodes = graph.nodes.filter(_.isRunning)
      val progressedNodes = runningNodes.map(progress)
      val statedNodes = graph.readyNodes.map(_.markRunning)
      val mergedNodes = graph.nodes
        .filter(n => !n.isRunning && !graph.readyNodes.contains(n)) ++
        statedNodes ++ progressedNodes
      val updatedExperiment = experiment.copy(graph = graph.copy(nodes = mergedNodes))
      if (updatedExperiment.graph.nodes.forall(_.isCompleted)) {
        experiments += id -> updatedExperiment.markCompleted
      } else {
        experiments += id -> updatedExperiment
      }
    }

  }

  private def progress(node: Node): Node = {
    if (node.isRunning) {
      if (random.nextDouble() > failureProbability) {
        val currentProgress = node.state.progress.get
        val nextProgress = Math.min(currentProgress.total, currentProgress.current + 10)
        if (nextProgress == currentProgress.total) {
          node.markCompleted(List.fill(node.operation.outArity)(UUID.randomUUID()))
        } else {
          node.withProgress(nextProgress)
        }
      } else {
        node.markFailed
      }
    } else {
      node
    }
  }

  private def enqueueNodes(experiment: Experiment): Experiment =
    experiment.copy(graph = experiment.graph.enqueueNodes)

  private def abortNodes(experiment: Experiment): Experiment =
    experiment.copy(graph = experiment.graph.abortNodes)
}

object MockRunningExperimentsActor {
  case class Tick()
}
