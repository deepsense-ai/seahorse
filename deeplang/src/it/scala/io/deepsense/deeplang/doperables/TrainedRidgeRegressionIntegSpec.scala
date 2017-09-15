/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 */

package io.deepsense.deeplang.doperables

import org.apache.spark.mllib.feature.StandardScalerModel
import org.apache.spark.mllib.linalg.{Vector => SparkVector, Vectors}
import org.apache.spark.mllib.regression.{GeneralizedLinearModel, RidgeRegressionModel}
import org.apache.spark.rdd.RDD
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer

class TrainedRidgeRegressionIntegSpec extends TrainedRegressionIntegSpec[RidgeRegressionModel] {

  private val scaledVectors = Seq(
    Vectors.dense(-0.1, 0.2),
    Vectors.dense(0.0, 0.4),
    Vectors.dense(0.1, -0.2))

  override def regressionName: String = "TrainedRidgeRegression"

  override val modelType: Class[RidgeRegressionModel] = classOf[RidgeRegressionModel]
  override val inputVectorsTransformer: (Seq[SparkVector]) => Seq[SparkVector] = a => scaledVectors
  override val regressionConstructor: (GeneralizedLinearModel, Seq[String], String) => Scorable =
    (model, featureColumns, targetColumn) => TrainedRidgeRegression(
      Some(model.asInstanceOf[RidgeRegressionModel]),
      Some(featureColumns),
      Some(targetColumn),
      Some(createScalerMock()))

  private def createScalerMock(): StandardScalerModel = {
    val mockScaler = mock[StandardScalerModel]
    when(mockScaler.transform(any[RDD[SparkVector]]())).thenAnswer(new Answer[RDD[SparkVector]] {
      override def answer(invocationOnMock: InvocationOnMock): RDD[SparkVector] = {
        val receivedRDD = invocationOnMock.getArgumentAt(0, classOf[RDD[SparkVector]])
        receivedRDD.collect() shouldBe inputVectors
        sparkContext.parallelize(scaledVectors)
      }
    })
    mockScaler
  }
}
