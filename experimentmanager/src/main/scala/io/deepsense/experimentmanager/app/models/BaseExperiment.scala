/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Wojciech Jurczyk
 */

package io.deepsense.experimentmanager.app.models

import io.deepsense.graph.Graph

/**
 * The common part of e.g. Experiment and InputExperiment models.
 * @param name Name of the experiment.
 * @param description Description of the experiment.
 * @param graph Experiment's graph. Never null but can be empty.
 */
abstract class BaseExperiment(name: String, description: String, graph: Graph)
