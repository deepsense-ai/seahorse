/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.experimentmanager.execution

import javax.inject.{Inject, Named}

import scala.collection.mutable
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

import akka.actor._
import akka.util.Timeout

import io.deepsense.commons.akka.RemoteAddressExtension
import io.deepsense.models.experiments.Experiment
import io.deepsense.models.experiments.Experiment.Id
import io.deepsense.models.messages.{Update, _}

class RunningExperimentsActor @Inject()(
    @Named("entitystorage.label") entitystorageLabel: String,
    @Named("runningexperiments.timeout") timeoutMillis: Long,
    gecMaker: (ActorRefFactory, String, String) => ActorRef)
  extends Actor with ActorLogging {

  val experiments = mutable.Map[Experiment.Id, (Experiment, ActorRef)]()

  implicit val timeout: Timeout = timeoutMillis.milliseconds

  override def receive: Receive = {
    case Get(eid) => get(eid)
    case Launch(experiment) => sender ! launch(experiment)
    case Abort(eid) => sender ! abort(eid)
    case GetAllByTenantId(tenantId) => getAllByTenantId(tenantId)
    case Delete(experimentId) => delete(experimentId)
    case Update(exp) => update(exp)
  }

  def update(experiment: Experiment): Unit = {
    log.info("Status Updated: {}", experiment.id)
    val gec = experiments(experiment.id)._2
    experiments(experiment.id) = (experiment, gec)
  }

  def launch(experiment: Experiment): Try[Experiment] = {
    log.info(">>> Launch({}) with {} nodes", experiment.id, experiment.graph.nodes.size)
    val isExperimentRunning = experiments.get(experiment.id).map(_._1).exists(_.isRunning)
    if (isExperimentRunning) {
      val reason = s"Experiment ${experiment.id} is already running. Rejecting request to launch"
      log.info("Rejected({}, {})", experiment.id, reason)
      Failure(new IllegalStateException(reason))
    } else {
      log.info("Launch accepted. Experiment is not running: {}", experiment.state.status)
      val gec = gecMaker(context, entitystorageLabel, experiment.id.toString)
      log.info("Created GEC actor: {}", gec)
      experiments.put(experiment.id, (experiment, gec))
      gec ! Launch(experiment)

      // FIXME It may not be started ever - it should come from GEC instead
      val runningExp = experiment.markRunning
      experiments.put(experiment.id, (runningExp, gec))
      Success(runningExp)
    }
  }

  private def delete(experimentId: Id): Unit = {
    for {
      (exp, _) <- experiments.get(experimentId)
      if exp.state.status != Experiment.Status.Running
    } experiments.remove(exp.id)
  }

  private def abort(id: Id): Try[Experiment] = {
    log.info(">>> {}", Abort(id))
    val result = experiments.get(id) match {
      case None =>
        val error = s"No experiment to abort $id"
        log.error(error)
        Failure(new IllegalArgumentException(error))
      case Some((experiment, client)) if experiment.isRunning =>
        // FIXME don't mark it aborted before it's really aborted
        val aborted = experiment.markAborted
        experiments.get(id).foreach { case (exp, gec) =>
          gec ! Abort(id)
        }
        experiments.put(aborted.id, (aborted, client))
        Success(aborted)
      case Some((experiment, client)) =>
        val error = s"Could not terminate experiment $id in state: ${experiment.state.status} (not Running)"
        log.error(error)
        Failure(new IllegalArgumentException(error))
    }
    log.info("<<< {} => {}", Abort(id), result)
    result
  }

  def get(eid: Experiment.Id): Unit = {
    log.info("Requesting status of an experiment: {}", eid)
    val experiment = experiments.get(eid).map(_._1)
    sender ! experiment
  }

  def getAllByTenantId(tenantId: Option[String]): Unit = {
    log.info(">>> GetAllByTenantId({})", tenantId)
    val experimentsByTenant = experiments.map(_._2._1)
      .groupBy(_.tenantId).mapValues(_.toSet)
    log.info(">>> GetAllByTenantId experiments: {}", tenantId)
    tenantId match {
      case Some(tenant) =>
        sender() ! ExperimentsMap(experimentsByTenant.filter(p => p._1 == tenant))
      case None => sender() ! ExperimentsMap(experimentsByTenant)
    }
  }
}

object RunningExperimentsActor {

  sealed trait Message

  case class Status(experiment: Option[Experiment]) extends Message

}
