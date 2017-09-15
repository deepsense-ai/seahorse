/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.doperations

import io.deepsense.deeplang.DOperation
import io.deepsense.deeplang.doperables.{Classifier, Scorable}

case class ScoreClassifier() extends Scorer[Classifier with Scorable] {
  override val id: DOperation.Id = "6f9a4e9e-fe1a-11e4-a322-1697f925ec7b"
  override val name = "Score classifier"
}
