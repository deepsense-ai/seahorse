/**
 * Copyright (c) 2015, CodiLime, Inc.
 */
package io.deepsense.experimentmanager

import java.util.concurrent.TimeUnit

import scala.concurrent.{ExecutionContext, Future}

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
import io.deepsense.experimentmanager.exceptions.{ExperimentNotFoundException, ExperimentRunningException}
import io.deepsense.experimentmanager.execution.RunningExperimentsActor._
import io.deepsense.experimentmanager.models.{Count, ExperimentsList}
import io.deepsense.experimentmanager.storage.ExperimentStorage
import io.deepsense.graph.Node
import io.deepsense.models.experiments.{Experiment, InputExperiment}

/**
 * Implementation of Experiment Manager
 */
class ExperimentManagerImpl @Inject()(
    authorizatorProvider: AuthorizatorProvider,
    storage: ExperimentStorage,
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
  extends ExperimentManager with Logging {

  implicit val runningExperimentsTimeout = Timeout(timeoutMillis, TimeUnit.MILLISECONDS)

  private def authorizator: Authorizator = authorizatorProvider.forContext(userContextFuture)

  def get(id: Id): Future[Option[Experiment]] = {
    logger.debug("Get experiment id: {}", id)
    authorizator.withRole(roleGet) { userContext =>
      val experiment = storage.get(id).flatMap {
        case Some(storedExperiment) =>
          val ownedExperiment = storedExperiment.assureOwnedBy(userContext)
          runningExperiment(id).map {
            case running: Some[Experiment] => running
            case None => Some(ownedExperiment)
          }
        case None => Future.successful(None)
      }
      experiment
    }
  }

  def update(experimentId: Id, experiment: InputExperiment): Future[Experiment] = {
    logger.debug("Update experiment id: {}, experiment: {}", experimentId, experiment)
    val now = DateTimeConverter.now
    authorizator.withRole(roleUpdate) { userContext =>
      val oldExperimentOption = storage.get(experimentId)
      oldExperimentOption.flatMap {
        case Some(oldExperiment) =>
          runningExperiment(experimentId).flatMap {
            case Some(runningExperiment)
              if runningExperiment.state.status == Experiment.Status.Running =>
                throw new ExperimentRunningException(experimentId)
            case _ =>
              runningExperimentsActor ! Delete(experimentId)
              val updatedExperiment = oldExperiment
                .assureOwnedBy(userContext)
                .updatedWith(experiment, now)
              storage.save(updatedExperiment)
          }
        case None => throw new ExperimentNotFoundException(experimentId)
      }
    }
  }

  def create(inputExperiment: InputExperiment): Future[Experiment] = {
    logger.debug("Create experiment inputExperiment: {}", inputExperiment)
    val now = DateTimeConverter.now
    authorizator.withRole(roleCreate) { userContext =>
      storage.save(inputExperiment.toExperimentOf(userContext, now))
    }
  }

  def experiments(
      limit: Option[Int],
      page: Option[Int],
      status: Option[Experiment.Status.Value]): Future[ExperimentsList] = {
    logger.debug("List experiments limit: {}, page: {}, status: {}", limit, page, status)
    authorizator.withRole(roleList) { userContext =>
      val tenantExperimentsFuture: Future[Seq[Experiment]] =
        storage.list(userContext, limit, page, status)
      val runningExperimentsFuture: Future[Map[Id, Experiment]] = runningExperimentsActor
        .ask(ExperimentsByTenant(Some(userContext.tenantId)))
        .mapTo[ExperimentsMap]
        .map(_.experimentsByTenantId.getOrElse(userContext.tenantId, Set())
          .map(experiment => experiment.id -> experiment).toMap)

      val runningAndStoredExperiments = for {
        tenantExperiments <- tenantExperimentsFuture
        runningExperiments <- runningExperimentsFuture
      } yield tenantExperiments
        .map(experiment => runningExperiments.getOrElse(experiment.id, experiment))

      runningAndStoredExperiments.map(rse => {
        ExperimentsList(Count(rse.size, rse.size), rse)
      })
    }
  }

  def delete(id: Id): Future[Boolean] = {
    logger.debug("Delete experiment id: {}", id)
    authorizator.withRole(roleDelete) { userContext =>
      storage.get(id).flatMap {
        case Some(experiment) =>
          experiment.assureOwnedBy(userContext)
          storage.delete(id).map(_ => true)
        case None => Future.successful(false)
      }
    }
  }

  def launch(
      id: Id,
      targetNodes: Seq[Node.Id]): Future[Experiment] = {
    logger.debug("Launch experiment id: {}, targetNodes: {}", id, targetNodes)
    authorizator.withRole(roleLaunch) { userContext =>
      val experimentFuture = storage.get(id)
      experimentFuture.flatMap {
        case Some(experiment) =>
          val ownedExperiment = experiment.assureOwnedBy(userContext)
          runningExperimentsActor
            .ask(Launch(ownedExperiment))
            .mapTo[Launched]
            .map(_.experiment)
        case None => throw new ExperimentNotFoundException(id)
      }
    }
  }

  def abort(id: Id, nodes: Seq[Node.Id]): Future[Experiment] = {
    logger.debug("Abort experiment id: {}, targetNodes: {}", id, nodes)
    authorizator.withRole(roleAbort) { userContext =>
      val experimentFuture = storage.get(id)
      experimentFuture.flatMap {
        case Some(experiment) =>
          val ownedExperiment = experiment.assureOwnedBy(userContext)
          runningExperimentsActor
            .ask(Abort(ownedExperiment.id))
            .mapTo[Status]
            .map(_ => ownedExperiment)
        case None => throw new ExperimentNotFoundException(id)
      }
    }
  }

  private def runningExperiment(id: Id): Future[Option[Experiment]] = {
    runningExperimentsActor
      .ask(GetStatus(id))
      .mapTo[Status]
      .map(_.experiment)
  }
}
