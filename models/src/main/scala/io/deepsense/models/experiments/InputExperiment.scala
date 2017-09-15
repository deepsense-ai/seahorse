/**
 * Copyright (c) 2015, CodiLime, Inc.
 */

package io.deepsense.models.experiments

import org.joda.time.DateTime

import io.deepsense.commons.auth.HasTenantId
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
  def toExperimentOf(owner: HasTenantId, creationDateTime: DateTime): Experiment = {
    Experiment(
      Experiment.Id.randomId  ,
      owner.tenantId,
      name,
      graph,
      creationDateTime,
      creationDateTime,
      description)
  }
}
