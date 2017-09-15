/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Wojciech Jurczyk
 */

package io.deepsense.experimentmanager.app

import scala.concurrent.{ExecutionContext, Future}

import com.google.inject.Inject
import com.google.inject.assistedinject.Assisted

import io.deepsense.experimentmanager.app.exceptions.ExperimentNotFoundException
import io.deepsense.experimentmanager.app.models.Graph.Node
import io.deepsense.experimentmanager.app.models.{Experiment, InputExperiment}
import io.deepsense.experimentmanager.app.storage.ExperimentStorage
import io.deepsense.experimentmanager.auth.usercontext.UserContext
import io.deepsense.experimentmanager.auth.{Authorizator, AuthorizatorProvider}


/**
 * Implementation of Experiment Manager
 */
class ExperimentManagerImpl @Inject()(
  authorizatorProvider: AuthorizatorProvider,
  @Assisted userContextFuture: Future[UserContext],
  storage: ExperimentStorage)
  (implicit ec: ExecutionContext) extends ExperimentManager {

  private def authorizator: Authorizator = authorizatorProvider.forContext(userContextFuture)

  def get(id: Experiment.Id): Future[Option[Experiment]] = {
    authorizator.withRole("experiment:get") { userContext =>
      storage.get(id).map {
        case Some(experiment) => Some(experiment.assureOwnedBy(userContext))
        case None => None
      }
    }
  }

  def update(experiment: Experiment): Future[Experiment] = {
    authorizator.withRole("experiment:update") { userContext =>
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
    authorizator.withRole("experiment:create") { userContext =>
      storage.save(inputExperiment.toExperimentOf(userContext))
    }
  }

  def experiments(
    limit: Option[Int],
    page: Option[Int],
    status: Option[Experiment.Status.Value]): Future[Seq[Experiment]] = {
    authorizator.withRole("experiments:list") { userContext =>
      storage.list(userContext, limit, page, status)
    }
  }

  def delete(id: Experiment.Id): Future[Boolean] = {
    authorizator.withRole("experiments:delete") { userContext =>
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
    authorizator.withRole("experiments:launch") { userContext =>
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
    authorizator.withRole("experiments:abort") { userContext =>
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
