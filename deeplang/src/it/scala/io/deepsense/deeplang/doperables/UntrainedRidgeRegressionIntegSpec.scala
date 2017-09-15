/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.doperables

import org.apache.spark.mllib.regression.{GeneralizedLinearAlgorithm, RidgeRegressionModel, RidgeRegressionWithSGD}
import org.scalactic.EqualityPolicy.Spread

class UntrainedRidgeRegressionIntegSpec
  extends UntrainedRegressionIntegSpec[RidgeRegressionModel] {

  val testDir: String = "/tests/UntrainedRidgeRegressionIntegSpec"

  override def regressionName: String = "UntrainedRidgeRegression"

  override def modelType: Class[RidgeRegressionModel] = classOf[RidgeRegressionModel]

  override def constructUntrainedModel: Trainable =
    UntrainedRidgeRegression(Some(mockUntrainedModel.asInstanceOf[RidgeRegressionWithSGD]))

  override val mockUntrainedModel: GeneralizedLinearAlgorithm[RidgeRegressionModel] =
    mock[RidgeRegressionWithSGD]

  override val featuresValues: Seq[Spread[Double]] = Seq(
    Spread(0.0, 0.0), -0.755 +- 0.01,
    Spread(0.0, 0.0), -0.377 +- 0.01,
    Spread(0.0, 0.0), 1.133 +- 0.01
  )

  override def validateResult(
    mockTrainedModel: RidgeRegressionModel,
    result: Scorable): Registration = {
    val castedResult = result.asInstanceOf[TrainedRidgeRegression]
    castedResult.model shouldBe Some(mockTrainedModel)
    castedResult.featureColumns shouldBe Some(Seq("column1", "column0"))
    castedResult.targetColumn shouldBe Some("column3")
  }
}
