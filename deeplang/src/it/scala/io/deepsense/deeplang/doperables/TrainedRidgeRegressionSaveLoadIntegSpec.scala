/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.doperables

import org.scalatest.BeforeAndAfter

import io.deepsense.deeplang.DeeplangIntegTestSupport
import io.deepsense.deeplang.doperables.factories.TrainedRidgeRegressionTestFactory

class TrainedRidgeRegressionSaveLoadIntegSpec
  extends DeeplangIntegTestSupport
  with BeforeAndAfter
  with TrainedRidgeRegressionTestFactory {

  private val testFilePath: String = "/tests/trainedRidgeRegressionSerializationTest"

  before {
    rawHdfsClient.delete(testFilePath, true)
  }

  "TrainedRidgeRegression" should {
    "save and load from HDFS" in {
      testTrainedRidgeRegression.save(executionContext)(testFilePath)

      val retrieved = TrainedRidgeRegression.loadFromHdfs(executionContext)(testFilePath)

      retrieved.model.get.intercept shouldBe testTrainedRidgeRegression.model.get.intercept
      retrieved.model.get.weights shouldBe testTrainedRidgeRegression.model.get.weights
      retrieved.featureColumns shouldBe testTrainedRidgeRegression.featureColumns
      retrieved.targetColumn  shouldBe testTrainedRidgeRegression.targetColumn
      retrieved.scaler.get.std shouldBe testTrainedRidgeRegression.scaler.get.std
      retrieved.scaler.get.mean shouldBe testTrainedRidgeRegression.scaler.get.mean
      retrieved.scaler.get.withStd shouldBe true
      retrieved.scaler.get.withMean shouldBe true
    }
  }
}
