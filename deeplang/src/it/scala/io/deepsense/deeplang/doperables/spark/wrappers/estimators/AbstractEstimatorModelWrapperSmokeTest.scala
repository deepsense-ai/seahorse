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

import org.apache.spark.mllib
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.sql.types.StructType

import io.deepsense.deeplang.DeeplangIntegTestSupport
import io.deepsense.deeplang.doperables.{Transformer, Estimator}
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.doperables.spark.wrappers.estimators.AbstractEstimatorModelWrapperSmokeTest.TestDataFrameRow
import io.deepsense.deeplang.params.ParamPair

abstract class AbstractEstimatorModelWrapperSmokeTest extends DeeplangIntegTestSupport {

  def className: String

  val estimator: Estimator[Transformer]

  val estimatorParams: Seq[ParamPair[_]]

  val dataFrame: DataFrame = {
    val rowSeq = Seq(
      TestDataFrameRow(0.0, 0.5, Vectors.dense(1.0, 2.0, 3.0), 0, 0, 0.2,
        Seq("a", "a", "a", "b", "b", "c").toArray),
      TestDataFrameRow(1.0, 2.0, Vectors.dense(4.0, 5.0, 6.0), 1, 1, 0.4,
        Seq("a", "b", "c", "d", "d", "d").toArray),
      TestDataFrameRow(1.0, 0.0, Vectors.dense(16.0, 11.0, 9.0), 2, 3, 0.4,
        Seq("a", "c", "d", "f", "f", "g").toArray)
    )
    val sparkDF = sqlContext.createDataFrame(rowSeq)
    DataFrame.fromSparkDataFrame(sparkDF)
  }

  def assertTransformedDF(dataFrame: DataFrame): Unit = {}
  def assertTransformedSchema(schema: StructType): Unit = {}

  className should {
    "successfully run _fit(), _transform() and _transformSchema()" in {
      val estimatorWithParams = estimator.set(estimatorParams: _*)
      val transformer = estimatorWithParams._fit(executionContext, dataFrame)
      val transformed = transformer._transform(executionContext, dataFrame)
       assertTransformedDF(transformed)
      val transformedSchema = transformer._transformSchema(dataFrame.sparkDataFrame.schema)
      assertTransformedSchema(transformedSchema.get)
    }
    "successfully run _fit_infer() and _transformSchema() with schema" in {
      val estimatorWithParams = estimator.set(estimatorParams: _*)
      val transformer = estimatorWithParams._fit_infer(Some(dataFrame.sparkDataFrame.schema))
      transformer._transformSchema(dataFrame.sparkDataFrame.schema)
    }
    "successfully run _fit_infer() without schema" in {
      val estimatorWithParams = estimator.set(estimatorParams: _*)
      estimatorWithParams._fit_infer(None)
    }
    "successfully run report" in {
      val estimatorWithParams = estimator.set(estimatorParams: _*)
      estimatorWithParams.report
    }
  }
}

object AbstractEstimatorModelWrapperSmokeTest {
  case class TestDataFrameRow(
    myLabel: Double,
    myWeight: Double,
    myFeatures: mllib.linalg.Vector,
    myItemId: Int,
    myUserId: Int,
    myRating: Double,
    myStringFeatures: Array[String])
}
