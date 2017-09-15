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

import org.scalatest.BeforeAndAfter

import io.deepsense.deeplang.DeeplangIntegTestSupport
import io.deepsense.deeplang.doperables.factories.TrainedRidgeRegressionTestFactory
import io.deepsense.deeplang.doperables.machinelearning.ridgeregression.TrainedRidgeRegression

class TrainedRidgeRegressionSaveLoadIntegSpec
  extends DeeplangIntegTestSupport
  with BeforeAndAfter
  with TrainedRidgeRegressionTestFactory {

  private val testFilePath: String = testsDir + "/trainedRidgeRegressionSerializationTest"

  before {
    fileSystemClient.delete(testFilePath)
    createDir(testsDir)
  }

  "TrainedRidgeRegression" should {
    "save and load from HDFS" in {
      testTrainedRidgeRegression.save(executionContext)(testFilePath)

      val retrieved = TrainedRidgeRegression.loadFromHdfs(executionContext)(testFilePath)

      retrieved.model.intercept shouldBe testTrainedRidgeRegression.model.intercept
      retrieved.model.weights shouldBe testTrainedRidgeRegression.model.weights
      retrieved.featureColumns shouldBe testTrainedRidgeRegression.featureColumns
      retrieved.targetColumn  shouldBe testTrainedRidgeRegression.targetColumn
      retrieved.scaler.std shouldBe testTrainedRidgeRegression.scaler.std
      retrieved.scaler.mean shouldBe testTrainedRidgeRegression.scaler.mean
      retrieved.scaler.withStd shouldBe true
      retrieved.scaler.withMean shouldBe true
    }
  }
}
