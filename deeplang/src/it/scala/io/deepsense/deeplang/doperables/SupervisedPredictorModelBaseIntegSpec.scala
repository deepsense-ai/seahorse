/**
 * Copyright 2015, deepsense.io
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

package io.deepsense.deeplang.doperables

trait SupervisedPredictorModelBaseIntegSpec extends PredictorModelBaseIntegSpec[Double] {

  self: ScorableBaseIntegSpec =>

  val featuresValues = Seq(10.0, 11.1, 12.2, 13.3).map(Seq(_))
  val predictionValues: Seq[Any] = Seq(0.0, 1.0, 2.0, 3.0)
}
