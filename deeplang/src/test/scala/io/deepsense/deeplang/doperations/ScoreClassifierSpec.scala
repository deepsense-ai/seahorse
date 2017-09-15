/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.doperations

import io.deepsense.deeplang.DOperation
import io.deepsense.deeplang.doperables.{Classifier, Scorable}

class ScoreClassifierSpec extends ScorerSpec[Scorable with Classifier] {

  override def scorer: DOperation = ScoreClassifier()

  override def scorerName: String = "ScoreClassifier"
}
