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
import org.apache.spark.mllib.regression.RidgeRegressionModel
import org.apache.spark.rdd.RDD
import org.mockito.AdditionalAnswers._
import org.mockito.Matchers.any
import org.mockito.Mockito._

import io.deepsense.deeplang.PrebuiltTypedColumns.ExtendedColumnType
import io.deepsense.deeplang.PrebuiltTypedColumns.ExtendedColumnType.ExtendedColumnType
import io.deepsense.deeplang.doperables.machinelearning.LinearRegressionParameters
import io.deepsense.deeplang.doperables.machinelearning.ridgeregression.TrainedRidgeRegression

class TrainedRidgeRegressionIntegSpec
  extends ScorableBaseIntegSpec("TrainedRidgeRegression")
  with PredictorModelBaseIntegSpec {

  override def acceptedFeatureTypes: Seq[ExtendedColumnType] = Seq(
    ExtendedColumnType.binaryValuedNumeric,
    ExtendedColumnType.nonBinaryValuedNumeric)

  override def unacceptableFeatureTypes: Seq[ExtendedColumnType] = Seq(
    ExtendedColumnType.categorical1,
    ExtendedColumnType.categorical2,
    ExtendedColumnType.categoricalMany,
    ExtendedColumnType.boolean,
    ExtendedColumnType.string,
    ExtendedColumnType.timestamp)

  override def mockTrainedModel(): PredictorSparkModel = {
    class RidgeRegressionPredictor
      extends RidgeRegressionModel(Vectors.dense(1), 1) with PredictorSparkModel {}
    mock[RidgeRegressionPredictor]
  }

  override def createScorableInstanceWithModel(trainedModelMock: PredictorSparkModel): Scorable =
    createScorableInstanceImpl(
      trainedModelMock.asInstanceOf[RidgeRegressionModel], mock[Seq[String]])

  override def createScorableInstance(features: String*): Scorable =
    createScorableInstanceImpl(new RidgeRegressionModel(Vectors.dense(1), 1), features)

  private def createScorableInstanceImpl(
      trainedModelMock: RidgeRegressionModel, features: Seq[String]): Scorable = {

    val scalerMock = mock[StandardScalerModel]

    doAnswer(returnsFirstArg())
      .when(scalerMock)
      .transform(any[RDD[SparkVector]])

    TrainedRidgeRegression(
      LinearRegressionParameters(1, 1, 1),
      trainedModelMock,
      features,
      targetColumnName,
      scalerMock)
  }
}
