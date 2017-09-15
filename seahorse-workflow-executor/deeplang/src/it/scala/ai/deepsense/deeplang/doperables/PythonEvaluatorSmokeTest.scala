/**
 * Copyright 2016 deepsense.ai (CodiLime, Inc)
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

import org.apache.spark.sql.Row
import org.apache.spark.sql.types.{DoubleType, StructField, StructType}
import org.mockito.Mockito._

import ai.deepsense.deeplang.params.ParamPair

class PythonEvaluatorSmokeTest extends AbstractEvaluatorSmokeTest {

  override def className: String = "PythonEvaluator"

  override val evaluator = new PythonEvaluator()

  override val evaluatorParams: Seq[ParamPair[_]] = Seq()

  override def setUpStubs(): Unit = {
    val someMetric = Seq[Row](Row(1.0))
    val metricDF = createDataFrame(someMetric, StructType(Seq(StructField("metric", DoubleType, nullable = false))))
    when(executionContext.dataFrameStorage.getOutputDataFrame(0)).thenReturn(Some(metricDF.sparkDataFrame))
  }
}
