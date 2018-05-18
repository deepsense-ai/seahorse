/**
 * Copyright 2015 deepsense.ai (CodiLime, Inc)
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

package ai.deepsense.deeplang.doperables

import scala.language.reflectiveCalls

import org.apache.spark.ml
import org.apache.spark.ml.param.{DoubleParam, ParamMap}
import org.apache.spark.sql.types.{StringType, StructField, StructType}
import org.apache.spark.sql

import ai.deepsense.deeplang.doperables.dataframe.DataFrame
import ai.deepsense.deeplang.doperables.report.Report
import ai.deepsense.deeplang.doperations.exceptions.ColumnDoesNotExistException
import ai.deepsense.deeplang.params.Param
import ai.deepsense.deeplang.params.selections.{NameSingleColumnSelection, SingleColumnSelection}
import ai.deepsense.deeplang.params.wrappers.spark.{DoubleParamWrapper, SingleColumnSelectorParamWrapper}
import ai.deepsense.deeplang.{DKnowledge, DeeplangTestSupport, ExecutionContext, UnitSpec}
import ai.deepsense.sparkutils.ML

class SparkEvaluatorWrapperSpec extends UnitSpec with DeeplangTestSupport {

  import SparkEvaluatorWrapperSpec._

  "SparkEvaluatorWrapper" should {
    "evaluate a DataFrame" in {
      val wrapper = new ExampleEvaluatorWrapper().setParamWrapper(metricValue)
      val inputDataFrame = mockInputDataFrame

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
      val inputDataFrame = mockInputDataFrame

      a[ColumnDoesNotExistException] should be thrownBy {
        wrapper._evaluate(mock[ExecutionContext], inputDataFrame)
      }
    }
    "validate params during inference" in {
      val wrapper = new ExampleEvaluatorWrapper().setColumnWrapper(
        NameSingleColumnSelection("invalid"))
      a[ColumnDoesNotExistException] should be thrownBy {
        wrapper._infer(DKnowledge(mockInputDataFrame))
      }
    }
  }

  def mockInputDataFrame: DataFrame = {
    val schema = StructType(Seq(
      StructField("column", StringType)
    ))
    createDataFrame(schema)
  }
}

object SparkEvaluatorWrapperSpec {

  val metricName = "test"
  val metricValue = 12.0

  case class ExampleEvaluatorWrapper() extends SparkEvaluatorWrapper[ExampleSparkEvaluator] {

    val paramWrapper = new DoubleParamWrapper[ExampleSparkEvaluator](
      "name",
      Some("description"),
      _.numericParam)
    setDefault(paramWrapper, 0.0)

    def setParamWrapper(value: Double): this.type = set(paramWrapper, value)

    val columnWrapper = new SingleColumnSelectorParamWrapper[
        ml.param.Params { val columnParam: ml.param.Param[String] }](
        name = "column",
        description = Some("Selected column."),
        sparkParamGetter = _.columnParam,
        portIndex = 0)
    setDefault(columnWrapper, NameSingleColumnSelection("column"))

    def setColumnWrapper(value: SingleColumnSelection): this.type = set(columnWrapper, value)

    override val params: Array[Param[_]] = Array(paramWrapper, columnWrapper)

    override def getMetricName: String = metricName

    override def report(extended: Boolean = true): Report = ???
  }

  class ExampleSparkEvaluator extends ML.Evaluator {

    def this(id: String) = this()

    override val uid: String = "evaluatorId"

    val numericParam = new DoubleParam(uid, "numeric", "description")
    val columnParam = new ml.param.Param[String](uid, "string", "description")

    def setNumericParam(value: Double): this.type = set(numericParam, value)
    def setColumnParam(value: String): this.type = set(columnParam, value)

    override def evaluateDF(dataset: sql.DataFrame): Double = {
      $(numericParam)
    }

    override def copy(extra: ParamMap): ml.evaluation.Evaluator = {
      defaultCopy(extra)
    }
  }
}
