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

import org.apache.spark.annotation.DeveloperApi
import org.apache.spark.ml
import org.apache.spark.ml.param.{DoubleParam, ParamMap}
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.{DataFrame => SparkDataFrame}
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar

import io.deepsense.deeplang.ExecutionContext
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.params.Param
import io.deepsense.deeplang.params.wrappers.spark.DoubleParamWrapper

object EstimatorModelWrappersFixtures extends MockitoSugar {

  def mockInputDataFrame(): DataFrame = {

    val sparkDataFrame = mock[SparkDataFrame]
    when(sparkDataFrame.schema).thenReturn(mock[StructType])

    val inputDataFrame = mock[DataFrame]
    when(inputDataFrame.sparkDataFrame).thenReturn(sparkDataFrame)

    inputDataFrame
  }

  class ExampleSparkEstimatorWrapper
    extends SparkEstimatorWrapper
      [ExampleSparkModel, ExampleSparkEstimator, ExampleSparkModelWrapper] {

    val numericParamWrapper = new DoubleParamWrapper[ExampleSparkEstimator](
      "name",
      "description",
      _.numericParam)

    def setNumericParamWrapper(value: Double): this.type = set(numericParamWrapper, value)

    override def report(executionContext: ExecutionContext): Report = ???
    override val params: Array[Param[_]] = declareParams(numericParamWrapper)
  }

  class ExampleSparkEstimator extends ml.Estimator[ExampleSparkModel] {

    def this(id: String) = this()

    override val uid: String = "estimatorId"

    val numericParam = new DoubleParam(uid, "name", "description")

    def setNumericParam(value: Double): this.type = set(numericParam, value)

    override def fit(dataset: SparkDataFrame): ExampleSparkModel = {
      require($(numericParam) == paramValueToSet)
      fitModel
    }

    @DeveloperApi
    override def transformSchema(schema: StructType): StructType = {
      require($(numericParam) == paramValueToSet)
      transformedSchema
    }

    override def copy(extra: ParamMap): ml.Estimator[ExampleSparkModel] = {
      defaultCopy(extra)
    }
  }

  class ExampleSparkModel extends ml.Model[ExampleSparkModel] {

    override val uid: String = "modelId"

    val numericParam = new DoubleParam(uid, "name", "description")

    def setNumericParam(value: Double): this.type = set(numericParam, value)

    override def copy(extra: ParamMap): ExampleSparkModel =
      extra.toSeq.foldLeft(new ExampleSparkModel())((model, paramPair) => model.set(paramPair))

    override def transform(dataset: SparkDataFrame): SparkDataFrame = {
      require($(numericParam) == paramValueToSet)
      fitDataFrame
    }

    @DeveloperApi
    override def transformSchema(schema: StructType): StructType = {
      require($(numericParam) == paramValueToSet)
      transformedSchema
    }
  }

  class ExampleSparkModelWrapper
    extends SparkModelWrapper[ExampleSparkModel, ExampleSparkEstimator] {

    val numericParamWrapper = new DoubleParamWrapper[ExampleSparkModel](
      "name",
      "description",
      _.numericParam)

    def setNumericParamParamWrapper(value: Double): this.type = set(numericParamWrapper, value)

    override def report(executionContext: ExecutionContext): Report = ???
    override val params: Array[Param[_]] = declareParams(numericParamWrapper)
  }

  val fitModel = new ExampleSparkModel()
  val fitDataFrame = mock[SparkDataFrame]
  val transformedSchema = mock[StructType]
  val paramValueToSet = 12.0
}
