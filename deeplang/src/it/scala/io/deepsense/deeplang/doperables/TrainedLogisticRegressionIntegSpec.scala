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
import org.apache.spark.mllib.linalg.{Vector => SparkVector}
import org.apache.spark.mllib.regression.GeneralizedLinearModel
import org.apache.spark.sql.Row
import org.apache.spark.sql.types.{DoubleType, StructField, StructType}
import org.mockito.Mockito.{times, verify, when}

import io.deepsense.deeplang.doperables.machinelearning.logisticregression.TrainedLogisticRegression

class TrainedLogisticRegressionIntegSpec
  extends TrainedRegressionIntegSpec[LogisticRegressionModel] {

  override def regressionName: String = "TrainedLogisticRegression"

  override val inputVectorsTransformer: (Seq[SparkVector]) => Seq[SparkVector] = identity

  override val regressionConstructor: (GeneralizedLinearModel, Seq[String], String) => Scorable =
    (model, features, target) => {
      val castedModel = model.asInstanceOf[LogisticRegressionModel]
      when(castedModel.clearThreshold()).thenReturn(castedModel)
      TrainedLogisticRegression(castedModel, features, target)
    }

  override val modelType: Class[LogisticRegressionModel] = classOf[LogisticRegressionModel]

  regressionName should {
    "clear Threshold on model" in {
      val model = mock[LogisticRegressionModel]
      when(model.clearThreshold()).thenReturn(model)

      val logisticRegression =
        TrainedLogisticRegression(model, Seq("f1", "f2"), "t")
      val df = createDataFrame(
        Seq(Row(1.0, 2.0, 3.0)),
        StructType(Seq(
          StructField("f1", DoubleType),
          StructField("f2", DoubleType),
          StructField("f3", DoubleType))))

      logisticRegression.score(executionContext)("prediction")(df)

      verify(model, times(1)).clearThreshold()
      ()
    }
  }
}
