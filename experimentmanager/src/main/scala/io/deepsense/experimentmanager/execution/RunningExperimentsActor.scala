package io.deepsense.experimentmanager.execution

import scala.collection.mutable

import akka.actor.{Actor, ActorLogging}

import io.deepsense.commons.models.Id
import io.deepsense.experimentmanager.execution.RunningExperimentsActor._
import io.deepsense.experimentmanager.models.Experiment
import io.deepsense.graphexecutor.GraphExecutorClient

class RunningExperimentsActor extends Actor with ActorLogging {
  val runningExperiments = mutable.Map[Id, GraphExecutorClient]()

  override def receive: Receive = {
    case Launch(experiment) => launch(experiment)
    case Abort(id) => abort(id)
    case GetStatus(id) => getStatus(id)
    case ListExperiments(tenantId) => listExperiments(tenantId)
    case x => unhandled(x)
  }

  // TODO Implement the methods below.

  private def launch(experiment: Experiment): Unit = {
    sender() ! Status(Some(experiment))
  }

  private def abort(id: Id): Unit = {
    sender() ! Status(None)
  }

  private def getStatus(id: Id): Unit = {
    sender() ! Status(None)
  }

  private def listExperiments(tenantId: Option[String]): Unit = {
    tenantId match {
      case Some(tenant) => sender() ! Experiments(Map(tenant -> Set()))
      case None => sender () ! Experiments(Map())
    }
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
