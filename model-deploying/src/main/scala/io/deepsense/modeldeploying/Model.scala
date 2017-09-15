/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 */

package io.deepsense.modeldeploying

case class Model(
    isLogistic: Boolean,
    intercept: Double,
    weights: Seq[Double],
    means: Seq[Double],
    stdDevs: Seq[Double]) {

  def score(getScoringRequest: GetScoringRequest): Double = {
    val centered = getScoringRequest.features.zip(means).map { case (f, m) => f - m }
    val scaled = centered.zip(stdDevs).map { case (c, sd) => if (sd == 0) 0 else c / sd }
    val dot = scaled.zip(weights).map { case (s, w) => s * w }
    val score = dot.sum + intercept
    if (isLogistic) sigmoid(score) else score
  }

  private def sigmoid(x: Double): Double = {
    return 1.0 / (1.0 + math.pow(math.E, -x))
  }

}
