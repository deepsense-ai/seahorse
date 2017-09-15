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

import scala.language.reflectiveCalls

import org.apache.spark.annotation.DeveloperApi
import org.apache.spark.ml
import org.apache.spark.ml.Estimator
import org.apache.spark.ml.param.{ParamMap, Param => SparkParam}
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.types.{IntegerType, StructField, StructType}

import io.deepsense.deeplang.ExecutionContext
import io.deepsense.deeplang.doperables.report.Report
import io.deepsense.deeplang.doperables.serialization.SerializableSparkModel
import io.deepsense.deeplang.doperables.{SparkEstimatorWrapper, SparkModelWrapper}
import io.deepsense.deeplang.params.wrappers.spark.SingleColumnCreatorParamWrapper
import io.deepsense.deeplang.params.{Param, Params}

object EstimatorModelWrapperFixtures {

  class SimpleSparkModel private[EstimatorModelWrapperFixtures]()
    extends ml.Model[SimpleSparkModel] {

    def this(x: String) = this()

    override val uid: String = "modelId"

    val predictionCol = new SparkParam[String](uid, "name", "description")

    def setPredictionCol(value: String): this.type = set(predictionCol, value)

    override def copy(extra: ParamMap): this.type = defaultCopy(extra)

    override def transform(dataset: DataFrame): DataFrame = {
      dataset.selectExpr("*", "1 as " + $(predictionCol))
    }

    @DeveloperApi
    override def transformSchema(schema: StructType): StructType = ???
  }

  class SimpleSparkEstimator extends ml.Estimator[SimpleSparkModel] {

    def this(x: String) = this()

    override val uid: String = "estimatorId"

    val predictionCol = new SparkParam[String](uid, "name", "description")

    override def fit(dataset: DataFrame): SimpleSparkModel =
      new SimpleSparkModel().setPredictionCol($(predictionCol))

    override def copy(extra: ParamMap): Estimator[SimpleSparkModel] = defaultCopy(extra)

    @DeveloperApi
    override def transformSchema(schema: StructType): StructType = {
      schema.add(StructField($(predictionCol), IntegerType, nullable = false))
    }
  }

  trait HasPredictionColumn extends Params {
    val predictionColumn = new SingleColumnCreatorParamWrapper[
        ml.param.Params { val predictionCol: SparkParam[String] }](
      "prediction column",
      "description",
      _.predictionCol)
    setDefault(predictionColumn, "abcdefg")

    def getPredictionColumn(): String = $(predictionColumn)
    def setPredictionColumn(value: String): this.type = set(predictionColumn, value)
  }

  class SimpleSparkModelWrapper
    extends SparkModelWrapper[SimpleSparkModel, SimpleSparkEstimator]
    with HasPredictionColumn {

    override val params: Array[Param[_]] = declareParams(predictionColumn)
    override def report: Report = ???

    override protected def loadModel(
      ctx: ExecutionContext,
      path: String): SerializableSparkModel[SimpleSparkModel] = ???
  }

  class SimpleSparkEstimatorWrapper
    extends SparkEstimatorWrapper[SimpleSparkModel, SimpleSparkEstimator, SimpleSparkModelWrapper]
    with HasPredictionColumn {

    override val params: Array[Param[_]] = declareParams(predictionColumn)
    override def report: Report = ???
  }
}
