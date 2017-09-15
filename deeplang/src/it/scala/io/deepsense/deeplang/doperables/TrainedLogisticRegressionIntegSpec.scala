/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.doperables

import org.apache.spark.mllib.classification.LogisticRegressionModel
import org.apache.spark.mllib.linalg.{Vector => SparkVector}
import org.apache.spark.mllib.regression.GeneralizedLinearModel
import org.apache.spark.sql.Row
import org.apache.spark.sql.types.{DoubleType, StructField, StructType}
import org.mockito.Mockito.{times, verify}

class TrainedLogisticRegressionIntegSpec
  extends TrainedRegressionIntegSpec[LogisticRegressionModel] {

  override def regressionName: String = "TrainedLogisticRegression"

  override val inputVectorsTransformer: (Seq[SparkVector]) => Seq[SparkVector] = identity

  override val regressionConstructor: (GeneralizedLinearModel, Seq[String], String) => Scorable =
    (model, features, target) => TrainedLogisticRegression(
      Some(model.asInstanceOf[LogisticRegressionModel]),
      Some(features),
      Some(target))

  override val modelType: Class[LogisticRegressionModel] = classOf[LogisticRegressionModel]

  regressionName should {
    "clear Threshold on model" in {
      val model = mock[LogisticRegressionModel]
      val logisticRegression =
        TrainedLogisticRegression(Some(model), Some(Seq("f1", "f2")), Some("t"))
      val df = createDataFrame(
        Seq(Row(1.0, 2.0, 3.0)),
        StructType(Seq(
          StructField("f1", DoubleType),
          StructField("f2", DoubleType),
          StructField("f3", DoubleType))))

      logisticRegression.score(executionContext)("prediction")(df)

      verify(model, times(1)).clearThreshold()
    }
  }
}
