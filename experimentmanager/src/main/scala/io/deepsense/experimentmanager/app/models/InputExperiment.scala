/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Wojciech Jurczyk
 */

package io.deepsense.experimentmanager.app.models

import java.util.UUID

import io.deepsense.experimentmanager.auth.HasTenantId
import io.deepsense.graph.Graph

/**
 * An experiment's representation used when creating a new experiment.
 * During creation of a new experiment API's client does not have to (and can not)
 * specify all fields that an experiment has. This class purpose is to avoid
 * optional fields in the experiment.
 */
case class InputExperiment(name: String, description: String = "", graph: Graph)
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
      graph,
      description)
  }
}
