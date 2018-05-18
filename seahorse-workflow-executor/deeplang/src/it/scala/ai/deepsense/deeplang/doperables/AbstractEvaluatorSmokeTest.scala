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

import org.apache.spark.sql.Row
import org.apache.spark.sql.types.{DoubleType, StringType, StructField, StructType}

import ai.deepsense.deeplang.doperables.dataframe.DataFrame
import ai.deepsense.deeplang.params.ParamPair
import ai.deepsense.deeplang.{DKnowledge, DeeplangIntegTestSupport}
import ai.deepsense.sparkutils.Linalg.Vectors

abstract class AbstractEvaluatorSmokeTest extends DeeplangIntegTestSupport {

  def className: String

  val evaluator: Evaluator

  val evaluatorParams: Seq[ParamPair[_]]

  val inputDataFrameSchema = StructType(Seq(
    StructField("s", StringType),
    StructField("prediction", DoubleType),
    StructField("rawPrediction", new ai.deepsense.sparkutils.Linalg.VectorUDT),
    StructField("label", DoubleType)
  ))

  val inputDataFrame: DataFrame = {
    val rowSeq = Seq(
      Row("aAa bBb cCc dDd eEe f", 1.0, Vectors.dense(2.1, 2.2, 2.3), 3.0),
      Row("das99213 99721 8i!#@!", 4.0, Vectors.dense(5.1, 5.2, 5.3), 6.0)
    )
    createDataFrame(rowSeq, inputDataFrameSchema)
  }

  def setUpStubs(): Unit = ()

  className should {
    "successfully run _evaluate()" in {
      setUpStubs()
      evaluator.set(evaluatorParams: _*)._evaluate(executionContext, inputDataFrame)
    }
    "successfully run _infer()" in {
      evaluator.set(evaluatorParams: _*)._infer(DKnowledge(inputDataFrame))
    }
    "successfully run report" in {
      evaluator.set(evaluatorParams: _*).report()
    }
  }
}
