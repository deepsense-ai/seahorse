/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Wojciech Jurczyk
 */

package io.deepsense.experimentmanager.app.models

import java.util.UUID

import io.deepsense.experimentmanager.auth.HasTenantId

/**
 * An experiment's representation used when creating a new experiment.
 */
case class InputExperiment(name: String, description: String = "", graph: Graph = Graph())
  extends BaseExperiment(name, description, graph) {

  /**
   * Creates an experiment upon the input experiment.
   * @param owner The owner of the created experiment.
   * @return An experiment owned by the owner.
   */
  def toExperimentOf(owner: HasTenantId): Experiment = {
    Experiment(
      UUID.randomUUID(),
      owner.tenantId,
      name,
      description,
      graph)
  }
}
