/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.models.messages

import io.deepsense.graph.Node
import io.deepsense.models.experiments.Experiment
import io.deepsense.models.experiments.Experiment._

sealed trait RunningExperimentsActorMessage

case class Launch(experiment: Experiment) extends RunningExperimentsActorMessage

case class ExecutorReady(experimentId: Experiment.Id) extends RunningExperimentsActorMessage

case class Abort(experimentId: Id) extends RunningExperimentsActorMessage

case class Get(experimentId: Id) extends RunningExperimentsActorMessage

case class GetAllByTenantId(tenantId: String) extends RunningExperimentsActorMessage

case class Update(experiment: Experiment) extends RunningExperimentsActorMessage

case class ExperimentsMap(experimentsByTenantId: Map[String, Set[Experiment]]) extends RunningExperimentsActorMessage

case class Delete(experimentId: Id) extends RunningExperimentsActorMessage

case class Completed(experiment: Experiment) extends RunningExperimentsActorMessage

case class NodeCompleted(experiment: Experiment, nodeId: Node.Id) extends RunningExperimentsActorMessage
