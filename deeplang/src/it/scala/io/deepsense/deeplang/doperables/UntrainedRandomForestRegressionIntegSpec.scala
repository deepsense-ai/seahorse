/**
 * Copyright 2015, CodiLime Inc.
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

import org.apache.spark.sql.Row
import org.apache.spark.sql.types.{DoubleType, StringType, StructField, StructType}
import org.scalactic.EqualityPolicy.Spread
import org.scalatest.BeforeAndAfter

import io.deepsense.deeplang.doperations.exceptions.{ColumnDoesNotExistException, ColumnsDoNotExistException, WrongColumnTypeException}
import io.deepsense.deeplang.parameters.{MultipleColumnSelection, NameColumnSelection, NameSingleColumnSelection}
import io.deepsense.deeplang.{DeeplangIntegTestSupport, ExecutionContext}

class UntrainedRandomForestRegressionIntegSpec
    extends DeeplangIntegTestSupport
    with BeforeAndAfter{

  def constructUntrainedModel: Trainable =
    UntrainedRandomForestRegression(mockUntrainedModel.asInstanceOf[RandomForestParameters])

  val mockUntrainedModel: RandomForestParameters =
    RandomForestParameters(1, "auto", "variance", 4, 100)

  val featuresValues: Seq[Spread[Double]] = Seq(
    Spread(0.0, 0.0), -0.755 +- 0.01,
    Spread(0.0, 0.0), -0.377 +- 0.01,
    Spread(0.0, 0.0), 1.133 +- 0.01
  )

  def validateResult(result: Scorable): Registration = {
    val castedResult = result.asInstanceOf[TrainedRandomForestRegression]
    castedResult.featureColumns shouldBe Some(Seq("column1", "column0"))
    castedResult.targetColumn shouldBe Some("column3")
  }

  "UntrainedRandomForestRegression" should {

    val inputRows: Seq[Row] = Seq(
      Row(-2.0, "x", -2.22, 1000.0),
      Row(-2.0, "y", -22.2, 2000.0),
      Row(-2.0, "z", -2222.0, 6000.0))

    val inputSchema: StructType = StructType(Seq(
      StructField("column1", DoubleType),
      StructField("column2", StringType),
      StructField("column3", DoubleType),
      StructField("column0", DoubleType))
    )

    lazy val inputDataFrame = createDataFrame(inputRows, inputSchema)

    "create model trained on given dataframe" in {
      val mockContext: ExecutionContext = mock[ExecutionContext]

      val regression = constructUntrainedModel
      val parameters = Trainable.Parameters(
        featureColumns = Some(MultipleColumnSelection(
          Vector(NameColumnSelection(Set("column0", "column1"))))),
        targetColumn = Some(NameSingleColumnSelection("column3")))

      val result = regression.train(mockContext)(parameters)(inputDataFrame)
      validateResult(result)
    }

    "throw an exception" when {
      "non-existing column was selected as target" in {
        intercept[ColumnDoesNotExistException] {
          val regression = constructUntrainedModel
          val parameters = Trainable.Parameters(
            featureColumns = Some(MultipleColumnSelection(
              Vector(NameColumnSelection(Set("column0", "column1"))))),
            targetColumn = Some(NameSingleColumnSelection("not exists")))
          regression.train(executionContext)(parameters)(inputDataFrame)
        }
        ()
      }
      "non-existing columns was selected as features" in {
        intercept[ColumnsDoNotExistException] {
          val regression = constructUntrainedModel
          val parameters = Trainable.Parameters(
            featureColumns = Some(MultipleColumnSelection(
              Vector(NameColumnSelection(Set("not exists", "column1"))))),
            targetColumn = Some(NameSingleColumnSelection("column3")))
          regression.train(executionContext)(parameters)(inputDataFrame)
        }
        ()
      }
      "not all selected features were Double" in {
        intercept[WrongColumnTypeException] {
          val regression = constructUntrainedModel
          val parameters = Trainable.Parameters(
            featureColumns = Some(MultipleColumnSelection(
              Vector(NameColumnSelection(Set("column2", "column1"))))),
            targetColumn = Some(NameSingleColumnSelection("column3")))
          regression.train(executionContext)(parameters)(inputDataFrame)
        }
        ()
      }
      "selected target was not Double" in {
        intercept[WrongColumnTypeException] {
          val regression = constructUntrainedModel
          val parameters = Trainable.Parameters(
            featureColumns = Some(MultipleColumnSelection(
              Vector(NameColumnSelection(Set("column0", "column1"))))),
            targetColumn = Some(NameSingleColumnSelection("column2")))
          regression.train(executionContext)(parameters)(inputDataFrame)
        }
        ()
      }
    }
  }
}
