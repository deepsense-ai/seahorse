/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Wojciech Jurczyk
 */

package io.deepsense.experimentmanager.app.models

/**
 * An experiment's representation used when creating a new experiment.
 */
case class InputExperiment(name: String, description: String = "", graph: Graph = Graph())
  extends BaseExperiment(name, description, graph)
