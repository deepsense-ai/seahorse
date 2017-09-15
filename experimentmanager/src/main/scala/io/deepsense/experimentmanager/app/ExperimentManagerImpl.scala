/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Wojciech Jurczyk
 */

package io.deepsense.experimentmanager.app

import scala.concurrent.{ExecutionContext, Future}

import com.google.inject.Inject
import com.google.inject.assistedinject.Assisted
import com.google.inject.name.Named

import io.deepsense.experimentmanager.app.exceptions.ExperimentNotFoundException
import io.deepsense.experimentmanager.app.models.{Experiment, InputExperiment}
import io.deepsense.experimentmanager.app.storage.ExperimentStorage
import io.deepsense.experimentmanager.auth.usercontext.UserContext
import io.deepsense.experimentmanager.auth.{Authorizator, AuthorizatorProvider}
import io.deepsense.graph.Node


/**
 * Implementation of Experiment Manager
 */
class ExperimentManagerImpl @Inject()(
  authorizatorProvider: AuthorizatorProvider,
  storage: ExperimentStorage,
  @Assisted userContextFuture: Future[UserContext],
  @Named("roles.get") roleGet: String,
  @Named("roles.update") roleUpdate: String,
  @Named("roles.create") roleCreate: String,
  @Named("roles.list") roleList: String,
  @Named("roles.delete") roleDelete: String,
  @Named("roles.launch") roleLaunch: String,
  @Named("roles.abort") roleAbort: String)
  (implicit ec: ExecutionContext) extends ExperimentManager {

  private def authorizator: Authorizator = authorizatorProvider.forContext(userContextFuture)

  def get(id: Experiment.Id): Future[Option[Experiment]] = {
    authorizator.withRole(roleGet) { userContext =>
      storage.get(id).map {
        case Some(experiment) => Some(experiment.assureOwnedBy(userContext))
        case None => None
      }
    }
  }

  def update(experiment: Experiment): Future[Experiment] = {
    authorizator.withRole(roleUpdate) { userContext =>
      val oldExperimentOption = storage.get(experiment.id)
      oldExperimentOption.flatMap {
        case Some(oldExperiment) =>
          val updatedExperiment = oldExperiment
            .assureOwnedBy(userContext)
            .updatedWith(experiment)
          storage.save(updatedExperiment)
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
    status: Option[Experiment.Status.Value]): Future[Seq[Experiment]] = {
    authorizator.withRole(roleList) { userContext =>
      storage.list(userContext, limit, page, status)
    }
  }

  def delete(id: Experiment.Id): Future[Boolean] = {
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
    id: Experiment.Id,
    targetNodes: List[Node.Id]): Future[Experiment] = {
    authorizator.withRole(roleLaunch) { userContext =>
      val experimentFuture = storage.get(id)
      experimentFuture.map {
        case Some(experiment) =>
          val ownedExperiment = experiment.assureOwnedBy(userContext)
          // graphExecutor.launch(...)
          // storage.save(...)
          ownedExperiment
        case None => throw new ExperimentNotFoundException(id)
      }
    }
  }

  def abort(id: Experiment.Id, nodes: List[Node.Id]): Future[Experiment] = {
    authorizator.withRole(roleAbort) { userContext =>
      val experimentFuture = storage.get(id)
      experimentFuture.map {
        case Some(experiment) =>
          val ownedExperiment = experiment.assureOwnedBy(userContext)
          // graphExecutor.abort(...)
          // storage.save(...)
          ownedExperiment
        case None => throw new ExperimentNotFoundException(id)
      }
    }
  }
}
