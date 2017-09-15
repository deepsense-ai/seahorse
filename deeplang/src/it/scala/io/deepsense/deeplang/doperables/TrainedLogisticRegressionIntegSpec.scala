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

import org.apache.spark.mllib.classification.LogisticRegressionModel
import org.apache.spark.mllib.linalg.Vectors
import org.mockito.Mockito._

import io.deepsense.deeplang.PrebuiltTypedColumns.ExtendedColumnType
import io.deepsense.deeplang.PrebuiltTypedColumns.ExtendedColumnType.ExtendedColumnType
import io.deepsense.deeplang.doperables.machinelearning.logisticregression.{LogisticRegressionParameters, TrainedLogisticRegression}

class TrainedLogisticRegressionIntegSpec
  extends ScorableBaseIntegSpec("TrainedLogisticRegression")
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
    class LogisticRegressionPredictor
      extends LogisticRegressionModel(Vectors.dense(1), 1) with PredictorSparkModel {}
    mock[LogisticRegressionPredictor]
  }

  override def createScorableInstanceWithModel(
      trainedModelMock: PredictorSparkModel): Scorable = {

    val model = trainedModelMock.asInstanceOf[LogisticRegressionModel]
    doReturn(model).when(model).clearThreshold()

    TrainedLogisticRegression(
      LogisticRegressionParameters(1, 1, 1),
      model,
      mock[Seq[String]],
      targetColumnName)
  }

  override def createScorableInstance(features: String*): Scorable = {
    TrainedLogisticRegression(
      LogisticRegressionParameters(1, 1, 1),
      new LogisticRegressionModel(Vectors.dense(1), 1),
      features,
      targetColumnName)
  }
}
