/**
 * Copyright 2015 deepsense.ai (CodiLime, Inc)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ai.deepsense.deeplang

import ai.deepsense.commons.models

case class Model(
    isLogistic: Boolean,
    intercept: Double,
    weights: Seq[Double],
    means: Seq[Double],
    stdDevs: Seq[Double]) {

  def score(features: Seq[Double]): Double = {
    val centered = features.zip(means).map { case (f, m) => f - m }
    val scaled = centered.zip(stdDevs).map { case (c, sd) => if (sd == 0) 0 else c / sd }
    val dot = scaled.zip(weights).map { case (s, w) => s * w }
    val score = dot.sum + intercept
    if (isLogistic) sigmoid(score) else score
  }

  private def sigmoid(x: Double): Double = {
    1.0 / (1.0 + math.pow(math.E, -x))
  }
}

object Model {
  type Id = models.Id
  val Id = models.Id
}
