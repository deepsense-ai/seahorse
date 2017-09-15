package io.deepsense.experimentmanager.app.execution

import scala.collection.mutable

import akka.actor.{Actor, ActorLogging}

import io.deepsense.experimentmanager.app.models.{Experiment, Id}
import io.deepsense.graphexecutor.GraphExecutorClient

class RunningExperimentsActor extends Actor with ActorLogging {
  val runningExperiments = mutable.Map[Id, GraphExecutorClient]()

  override def receive: Receive = {
    case x =>
      // TODO
      println("Unhandled: " + x)
      unhandled(x)
  }
}

object RunningExperimentsActor {
  case class Launch(experiment: Experiment)
  case class Abort(experimentId: Id)
  case class GetStatus(experimentId: Id)
  case class ListExperiments(tenantId: Option[String])
  case class Status(experiment: Option[Experiment])
  case class Experiments(experimentsByTenantId: Map[String, Set[Experiment]])
}
