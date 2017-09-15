/**
 * Copyright (c) 2015, CodiLime Inc.
 */
package io.deepsense.experimentmanager.models

import io.deepsense.models.experiments.Experiment

case class ExperimentsList(count: Count, experiments: Seq[Experiment])

case class Count(all: Int, filtered: Int)
