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

package ai.deepsense.deeplang.doperables.spark.wrappers.estimators

import ai.deepsense.deeplang.params.ParamPair
import ai.deepsense.deeplang.params.selections.NameSingleColumnSelection

class ALSSmokeTest extends AbstractEstimatorModelWrapperSmokeTest {

  override def className: String = "ALS"

  override val estimator = new ALS()

  import estimator._

  override val estimatorParams: Seq[ParamPair[_]] = Seq(
    alpha -> 1.0,
    checkpointInterval -> 15.0,
    implicitPrefs -> false,
    itemColumn -> NameSingleColumnSelection("myItemId"),
    maxIterations -> 5.0,
    nonnegative -> false,
    numItemBlocks -> 10.0,
    numUserBlocks -> 10.0,
    predictionColumn -> "prediction",
    rank -> 8.0,
    ratingColumn -> NameSingleColumnSelection("myRating"),
    regularizationParam -> 0.2,
    seed -> 100.0,
    userColumn -> NameSingleColumnSelection("myUserId")
  )
}
