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

import org.apache.spark.mllib.linalg.{Vector => SparkVector}
import org.apache.spark.mllib.tree.model.GradientBoostedTreesModel
import org.apache.spark.rdd.RDD
import org.mockito.Matchers._
import org.mockito.Mockito._

import io.deepsense.deeplang.doperables.machinelearning.gradientboostedtrees.GradientBoostedTreesParameters
import io.deepsense.deeplang.doperables.machinelearning.gradientboostedtrees.regression.TrainedGradientBoostedTreesRegression


class TrainedGradientBoostedTreesRegressionIntegSpec extends TrainedTreeRegressionIntegSpec {

  override def trainedRegressionName: String = "TrainedGradientBoostedTreesRegression"

  override def createMockTrainedRegression(
    featureColumnNames: Seq[String],
    targetColumnName: String,
    resultDoubles: Seq[Double]): Scorable = {

    val mockModelParameters = mock[GradientBoostedTreesParameters]

    val mockModel = createRegressionModelMock(
      expectedInput = inputVectors,
      output = resultDoubles)

    TrainedGradientBoostedTreesRegression(
      mockModelParameters, mockModel, featureColumnNames, targetColumnName)
  }

  private def createRegressionModelMock(
    expectedInput: Seq[SparkVector],
    output: Seq[Double]): GradientBoostedTreesModel = {

    val mockModel = mock[GradientBoostedTreesModel]

    when(mockModel.predict(any[RDD[SparkVector]]())).thenAnswer(
      constructRegressionModelMockAnswer(expectedInput, output))

    mockModel
  }
}
