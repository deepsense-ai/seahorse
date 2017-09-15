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

import org.apache.spark.mllib.feature.StandardScalerModel
import org.apache.spark.mllib.linalg.{Vector => SparkVector, Vectors}
import org.apache.spark.mllib.regression.{GeneralizedLinearModel, RidgeRegressionModel}
import org.apache.spark.rdd.RDD
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer

import io.deepsense.deeplang.doperables.machinelearning.ridgeregression.TrainedRidgeRegression

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
      model.asInstanceOf[RidgeRegressionModel],
      featureColumns,
      targetColumn,
      createScalerMock())

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
