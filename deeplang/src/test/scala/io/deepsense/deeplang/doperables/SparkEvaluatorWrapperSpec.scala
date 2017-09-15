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

import org.apache.spark.ml
import org.apache.spark.ml.param.{DoubleParam, ParamMap}
import org.apache.spark.sql.types.{StringType, StructField, StructType}
import org.apache.spark.sql.{DataFrame => SparkDataFrame}
import org.mockito.Mockito._

import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.doperables.report.Report
import io.deepsense.deeplang.doperations.exceptions.ColumnDoesNotExistException
import io.deepsense.deeplang.params.Param
import io.deepsense.deeplang.params.selections.{NameSingleColumnSelection, SingleColumnSelection}
import io.deepsense.deeplang.params.wrappers.spark.{DoubleParamWrapper, SingleColumnSelectorParamWrapper}
import io.deepsense.deeplang.{DKnowledge, ExecutionContext, UnitSpec}

class SparkEvaluatorWrapperSpec extends UnitSpec {

  import SparkEvaluatorWrapperSpec._

  "SparkEvaluatorWrapper" should {
    "evaluate a DataFrame" in {
      val wrapper = new ExampleEvaluatorWrapper().setParamWrapper(metricValue)
      val inputDataFrame = mockInputDataFrame()

      val value = wrapper._evaluate(mock[ExecutionContext], inputDataFrame)
      value shouldBe MetricValue("test", metricValue)
    }
    "infer knowledge" in {
      val wrapper = new ExampleEvaluatorWrapper().setParamWrapper(metricValue)
      val inferredValue = wrapper._infer(DKnowledge(DataFrame.forInference()))
      inferredValue.name shouldBe metricName
    }
    "validate params" in {
      val wrapper = new ExampleEvaluatorWrapper().setColumnWrapper(
        NameSingleColumnSelection("invalid"))
      val inputDataFrame = mockInputDataFrame()

      a[ColumnDoesNotExistException] should be thrownBy {
        wrapper._evaluate(mock[ExecutionContext], inputDataFrame)
      }
    }
    "validate params during inference" in {
      val wrapper = new ExampleEvaluatorWrapper().setColumnWrapper(
        NameSingleColumnSelection("invalid"))
      a[ColumnDoesNotExistException] should be thrownBy {
        wrapper._infer(DKnowledge(mockInputDataFrame()))
      }
    }
  }

  def mockInputDataFrame(): DataFrame = {

    val schema = StructType(Seq(
      StructField("column", StringType)
    ))

    val sparkDataFrame = mock[SparkDataFrame]
    when(sparkDataFrame.schema).thenReturn(schema)

    val inputDataFrame = mock[DataFrame]
    when(inputDataFrame.sparkDataFrame).thenReturn(sparkDataFrame)

    when(inputDataFrame.schema).thenReturn(Some(schema))

    inputDataFrame
  }
}

object SparkEvaluatorWrapperSpec {

  val metricName = "test"
  val metricValue = 12.0

  case class ExampleEvaluatorWrapper() extends SparkEvaluatorWrapper[ExampleSparkEvaluator] {

    val paramWrapper = new DoubleParamWrapper[ExampleSparkEvaluator](
      "name",
      "description",
      _.numericParam)
    setDefault(paramWrapper, 0.0)

    def setParamWrapper(value: Double): this.type = set(paramWrapper, value)

    val columnWrapper = new SingleColumnSelectorParamWrapper[
        ml.param.Params { val columnParam: ml.param.Param[String] }](
        name = "column",
        description = "Selected column.",
        sparkParamGetter = _.columnParam,
        portIndex = 0)
    setDefault(columnWrapper, NameSingleColumnSelection("column"))

    def setColumnWrapper(value: SingleColumnSelection): this.type = set(columnWrapper, value)

    override val params: Array[Param[_]] = declareParams(paramWrapper, columnWrapper)

    override def getMetricName: String = metricName

    override def report: Report = ???
  }

  class ExampleSparkEvaluator extends ml.evaluation.Evaluator {

    def this(id: String) = this()

    override val uid: String = "evaluatorId"

    val numericParam = new DoubleParam(uid, "numeric", "description")
    val columnParam = new ml.param.Param[String](uid, "string", "description")

    def setNumericParam(value: Double): this.type = set(numericParam, value)
    def setColumnParam(value: String): this.type = set(columnParam, value)

    override def evaluate(dataset: SparkDataFrame): Double = {
      $(numericParam)
    }

    override def copy(extra: ParamMap): ml.evaluation.Evaluator = {
      defaultCopy(extra)
    }
  }
}
