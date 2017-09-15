/**
 * Copyright 2016 deepsense.ai (CodiLime, Inc)
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

package ai.deepsense.deeplang.doperables.spark.wrappers.estimators.stringindexingwrapper

import ai.deepsense.deeplang.UnitSpec
import ai.deepsense.deeplang.doperables.spark.wrappers.estimators.{GBTClassifier, VanillaGBTClassifier}

class StringIndexingWrapperSpec extends UnitSpec {

  "String indexing wrapper" should {
    "inherit default values from wrapped estimator" in {
      val someWrappedEstimator = new VanillaGBTClassifier()
      val someWrapper = new GBTClassifier

      for (someParam <- someWrapper.params) {
        someWrapper.getDefault(someParam) shouldEqual someWrappedEstimator.getDefault(someParam)
      }
    }
  }

}
