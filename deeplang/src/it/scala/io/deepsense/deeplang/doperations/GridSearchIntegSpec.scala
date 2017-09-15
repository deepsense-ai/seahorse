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

package io.deepsense.deeplang.doperations

import org.apache.spark.ml
import org.apache.spark.ml.evaluation
import org.apache.spark.ml.tuning.{CrossValidator, ParamGridBuilder}
import org.apache.spark.mllib.linalg
import org.apache.spark.mllib.linalg.Vectors
import spray.json._

import io.deepsense.deeplang.DeeplangIntegTestSupport
import io.deepsense.deeplang.doperables.Report
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.doperables.spark.wrappers.estimators.LinearRegression
import io.deepsense.deeplang.doperables.spark.wrappers.evaluators.RegressionEvaluator

class GridSearchIntegSpec extends DeeplangIntegTestSupport with DefaultJsonProtocol {

  private val regularizationParams = Array(0.01, 0.5, 5.0)

  "GridSearch" should {
    "find best params" in {
      val gridSearch = new GridSearch()
      val estimator = new LinearRegression()
      val dataFrame = buildDataFrame()
      val evaluator = new RegressionEvaluator()
      gridSearch.setEstimatorParams(estimatorParams)
      gridSearch.setNumberOfFolds(2)

      val results = gridSearch.execute(executionContext)(Vector(dataFrame, estimator, evaluator))
      val report = results.head.asInstanceOf[Report]

      val tables = report.content.tables

      val bestMetricsTable = tables.head
      bestMetricsTable.values.size shouldBe 1
      bestMetricsTable.values shouldBe List(List(Some("10.0"), Some("5.0"), Some("95104.573311")))

      val expectedMetrics = List(
        List(Some("10.0"), Some("5.0"), Some("95104.573311")),
        List(Some("10.0"), Some("0.5"), Some("95105.477058")),
        List(Some("10.0"), Some("0.01"), Some("95105.575481")))
      val metricsTable = tables(1)
      metricsTable.values.size shouldBe 3
      metricsTable.values shouldBe expectedMetrics
    }
  }

  private val estimatorParams = JsObject(
    "regularization param" -> seqParam(Seq(0.01, 0.5, 5.0)),
    "features column" -> JsObject(
      "type" -> JsString("column"),
      "value" -> JsString("features")
    ),
    "max iterations" -> JsNumber(10.0)
  )

  private def seqParam(values: Seq[Double]): JsObject = {
    JsObject(
      "values" -> JsArray(
        JsObject(
          "type" -> JsString("seq"),
          "value" -> JsObject(
            "sequence" -> values.toJson)
        )
      )
    )
  }

  private def buildDataFrame(): DataFrame = {
    val districtFactors = Seq(0.6, 0.8, 1.0)
    val priceForMeterSq = 7000
    val apartments = Range(40, 300, 5).map { case flatSize =>
      val districtFactor = districtFactors(flatSize % districtFactors.length)
      Apartment(
        Vectors.dense(flatSize, districtFactor),
        (flatSize * districtFactor * priceForMeterSq).toLong)
    }
    DataFrame.fromSparkDataFrame(sqlContext.createDataFrame(apartments))
  }

  // Pure spark implementation which can be used to validate deeplang GridSearch results
  private def executePureSparkImplementation(): Array[Double] = {
    val lr = new ml.regression.LinearRegression()
    val paramGrid = new ParamGridBuilder()
      .addGrid(lr.regParam, regularizationParams)
      .build()
    val cv = new CrossValidator()
      .setEstimator(lr)
      .setEvaluator(new evaluation.RegressionEvaluator())
      .setEstimatorParamMaps(paramGrid)
      .setNumFolds(2)
    val cvModel = cv.fit(buildDataFrame().sparkDataFrame)
    cvModel.avgMetrics
  }
}

private case class Apartment(features: linalg.Vector, label: Double)
