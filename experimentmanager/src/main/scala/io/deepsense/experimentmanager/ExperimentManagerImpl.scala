/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Wojciech Jurczyk
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
import io.deepsense.commons.models.Id
import io.deepsense.experimentmanager.exceptions.{ExperimentNotFoundException, ExperimentRunningException}
import io.deepsense.experimentmanager.execution.RunningExperimentsActor._
import io.deepsense.experimentmanager.models.{Count, ExperimentsList, Experiment, InputExperiment}
import io.deepsense.experimentmanager.storage.ExperimentStorage
import io.deepsense.graph.Node

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
  (implicit ec: ExecutionContext) extends ExperimentManager {

  implicit val runningExperimentsTimeout = Timeout(timeoutMillis, TimeUnit.MILLISECONDS)

  private def authorizator: Authorizator = authorizatorProvider.forContext(userContextFuture)

  def get(id: Id): Future[Option[Experiment]] = {
    authorizator.withRole(roleGet) { userContext =>
      val experiment = storage.get(id).flatMap {
        case Some(storedExperiment) => {
          val ownedExperiment = storedExperiment.assureOwnedBy(userContext)
          runningExperiment(id).map {
            case running: Some[Experiment] => running
            case None => Some(ownedExperiment)
          }
        }
        case None => Future.successful(None)
      }
      experiment
    }
  }

  def update(experiment: Experiment): Future[Experiment] = {
    authorizator.withRole(roleUpdate) { userContext =>
      val oldExperimentOption = storage.get(experiment.id)
      oldExperimentOption.flatMap {
        case Some(oldExperiment) =>
          runningExperiment(experiment.id).flatMap {
            case Some(runningExperiment) /* if runningExperiment.status == InDraft */ =>
              throw new ExperimentRunningException(experiment.id)
            case _ =>
              val updatedExperiment = oldExperiment
                .assureOwnedBy(userContext)
                .updatedWith(experiment)
              storage.save(updatedExperiment)
          }
        case None => throw new ExperimentNotFoundException(experiment.id)
      }
    }
  }

  def create(inputExperiment: InputExperiment): Future[Experiment] = {
    authorizator.withRole(roleCreate) { userContext =>
      storage.save(inputExperiment.toExperimentOf(userContext))
    }
  }

  def experiments(
    limit: Option[Int],
    page: Option[Int],
    status: Option[Experiment.Status.Value]): Future[ExperimentsList] = {
    authorizator.withRole(roleList) { userContext =>
      val tenantExperimentsFuture: Future[List[Experiment]] =
        storage.list(userContext, limit, page, status)
      val runningExperimentsFuture: Future[Map[Id, Experiment]] = runningExperimentsActor
        .ask(ListExperiments(Some(userContext.tenantId)))
        .mapTo[Experiments]
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
    targetNodes: List[Node.Id]): Future[Experiment] = {
    authorizator.withRole(roleLaunch) { userContext =>
      val experimentFuture = storage.get(id)
      experimentFuture.flatMap {
        case Some(experiment) =>
          val ownedExperiment = experiment.assureOwnedBy(userContext)
          runningExperimentsActor
            .ask(Launch(ownedExperiment))
            .mapTo[Status]
            .map(_.experiment.get)
        case None => throw new ExperimentNotFoundException(id)
      }
    }
  }

  def abort(id: Id, nodes: List[Node.Id]): Future[Experiment] = {
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
