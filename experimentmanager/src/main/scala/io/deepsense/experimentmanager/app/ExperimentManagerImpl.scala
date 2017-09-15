/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Wojciech Jurczyk
 */

package io.deepsense.experimentmanager.app

import java.util.UUID

import scala.collection.immutable.IndexedSeq
import scala.collection.mutable
import scala.concurrent._

import com.google.inject.Inject

import io.deepsense.experimentmanager.app.models.Experiment.{Id, Status}
import io.deepsense.experimentmanager.app.models.Graph.Node
import io.deepsense.experimentmanager.app.models.{Experiment, Graph, InputExperiment}

/**
 * Implementation of Experiment Manager (currently a mock)
 */
class ExperimentManagerImpl @Inject()(implicit executionContext: ExecutionContext)
  extends ExperimentManager {
  private val experiments: mutable.Set[Experiment] = mutable.Set(
    Experiment(UUID.randomUUID(), "A mocked experiment", "A mocked experiment1", Graph()),
    Experiment(UUID.randomUUID(), "A mocked experiment", "A mocked experiment2", Graph()),
    Experiment(UUID.randomUUID(), "A mocked experiment", "A mocked experiment3", Graph()))

  override def get(id: Id): Future[Option[Experiment]] = future {
    experiments.find(_.id == id)
  }

  override def delete(id: Id): Unit = {
    experiments.find(_.id == id).map(experiments.remove)
  }

  override def list(
     limit: Option[Int],
     page: Option[Int],
     status: Option[Status.Value]): Future[IndexedSeq[Experiment]] = future {
    experiments.toIndexedSeq
  }

  override def launch(
      id: Id,
      targetNodes: List[Node.Id]): Future[Experiment] = {
    get(id).map(_.get)
  }

  override def abort(id: Id, nodes: List[Node.Id]): Future[Experiment] = {
    get(id).map(_.get)
  }

  override def update(experiment: Experiment): Future[Experiment] = future {
    // TODO check if exists
    experiment
  }

  override def create(input: InputExperiment): Future[Experiment] = future {
    val created = Experiment(UUID.randomUUID(), input.name, input.description, input.graph)
    experiments += created
    created
  }
}
