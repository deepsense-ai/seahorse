/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.models.workflows

import io.deepsense.graph.Graph

/**
 * The common part of e.g. Experiment and InputExperiment models.
 * @param name Name of the experiment.
 * @param description Description of the experiment.
 * @param graph Experiment's graph. Never null but can be empty.
 */
@SerialVersionUID(1)
abstract class BaseWorkflow(name: String, description: String, graph: Graph) extends Serializable
