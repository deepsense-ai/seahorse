/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Wojciech Jurczyk
 */

package io.deepsense.experimentmanager.models

case class ExperimentsList(count: Count, experiments: List[Experiment])

case class Count(all: Int, filtered: Int)
