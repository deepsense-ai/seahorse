/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.doperables

import org.apache.spark.mllib.classification.{LogisticRegressionWithLBFGS, LogisticRegressionModel}
import org.apache.spark.mllib.regression.GeneralizedLinearAlgorithm
import org.scalactic.EqualityPolicy.Spread

class UntrainedLogisticRegressionIntegSpec
  extends UntrainedRegressionIntegSpec[LogisticRegressionModel] {

  val testDir: String = "/tests/UntrainedLogisticRegressionIntegSpec"

  override def regressionName: String = "UntrainedLogisticRegression"

  override def modelType: Class[LogisticRegressionModel] = classOf[LogisticRegressionModel]

  override def constructUntrainedModel: Trainable =
    UntrainedLogisticRegression(Some(mockUntrainedModel.asInstanceOf[LogisticRegressionWithLBFGS]))

  override val mockUntrainedModel: GeneralizedLinearAlgorithm[LogisticRegressionModel] =
    mock[LogisticRegressionWithLBFGS]

  override val featuresValues: Seq[Spread[Double]] = Seq(
    Spread(-2.0, 0.0),
    Spread(1000.0, 0.0),
    Spread(-2.0, 0.0),
    Spread(2000.0, 0.0),
    Spread(-2.0, 0.0),
    Spread(6000.0, 0.0))

  override def validateResult(
      mockTrainedModel: LogisticRegressionModel,
      result: Scorable): Registration = {
    val castedResult = result.asInstanceOf[TrainedLogisticRegression]
    castedResult.model shouldBe Some(mockTrainedModel)
    castedResult.featureColumns shouldBe Some(Seq("column1", "column0"))
    castedResult.targetColumn shouldBe Some("column3")
  }
}
