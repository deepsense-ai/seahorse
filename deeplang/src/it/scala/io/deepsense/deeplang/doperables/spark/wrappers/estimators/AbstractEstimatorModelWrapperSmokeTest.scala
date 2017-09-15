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

package io.deepsense.deeplang.doperables.spark.wrappers.estimators

import io.deepsense.deeplang.DeeplangIntegTestSupport
import io.deepsense.deeplang.doperables.SparkEstimatorWrapper
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.doperables.spark.wrappers.estimators.AbstractEstimatorModelWrapperSmokeTest.TestDataFrameRow
import io.deepsense.deeplang.params.ParamPair
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.{ml, mllib}

abstract class AbstractEstimatorModelWrapperSmokeTest[E <: ml.Estimator[_]]
  extends DeeplangIntegTestSupport {

  def className: String

  val estimatorWrapper: SparkEstimatorWrapper[_, E, _]

  val estimatorParams: Seq[ParamPair[_]]

  val dataFrame: DataFrame = {
    val rowSeq = Seq(
      TestDataFrameRow(0.0, Vectors.dense(1.0, 2.0, 3.0)),
      TestDataFrameRow(1.0, Vectors.dense(4.0, 5.0, 6.0))
    )
    val sparkDF = sqlContext.createDataFrame(rowSeq)
    DataFrame.fromSparkDataFrame(sparkDF)
  }

  className should {
    "successfully run _fit(), _transform() and _transformSchema()" in {
      val estimatorWithParams = estimatorWrapper.set(estimatorParams: _*)
      val transformer = estimatorWithParams._fit(dataFrame)
      transformer._transform(executionContext, dataFrame)
      transformer._transformSchema(dataFrame.sparkDataFrame.schema)
    }
    "successfully run _fit_infer() and _transformSchema() with schema" in {
      val estimatorWithParams = estimatorWrapper.set(estimatorParams: _*)
      val transformer = estimatorWithParams._fit_infer(Some(dataFrame.sparkDataFrame.schema))
      transformer._transformSchema(dataFrame.sparkDataFrame.schema)
    }
    "successfully run _fit_infer() without schema" in {
      val estimatorWithParams = estimatorWrapper.set(estimatorParams: _*)
      val transformer = estimatorWithParams._fit_infer(None)
    }
  }
}

object AbstractEstimatorModelWrapperSmokeTest {
  case class TestDataFrameRow(myLabel: Double, myFeatures: mllib.linalg.Vector)
}
