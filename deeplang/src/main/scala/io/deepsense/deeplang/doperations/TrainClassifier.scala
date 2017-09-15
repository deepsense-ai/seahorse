/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.doperations

import io.deepsense.deeplang.DOperation
import io.deepsense.deeplang.doperables.{Scorable, Trainable, Classifier}

case class TrainClassifier() extends Trainer[Classifier with Trainable, Classifier with Scorable] {
  override val id: DOperation.Id = "892cf942-fe24-11e4-a322-1697f925ec7b"
  override val name = "Train Classifier"
}
