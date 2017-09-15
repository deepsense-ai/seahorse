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
import org.apache.spark.sql.types._
import org.scalactic.EqualityPolicy.Spread
import org.scalatest.BeforeAndAfter

import io.deepsense.deeplang.doperables.dataframe.types.categorical.{CategoriesMapping, MappingMetadataConverter}
import io.deepsense.deeplang.doperations.exceptions.{ColumnDoesNotExistException, ColumnsDoNotExistException, WrongColumnTypeException}
import io.deepsense.deeplang.parameters.{MultipleColumnSelection, NameColumnSelection, NameSingleColumnSelection}
import io.deepsense.deeplang.{DeeplangIntegTestSupport, ExecutionContext}

class UntrainedRandomForestClassificationIntegSpec
    extends DeeplangIntegTestSupport
    with BeforeAndAfter{

  def constructUntrainedModel: Trainable =
    UntrainedRandomForestClassification(mockUntrainedModel.asInstanceOf[RandomForestParameters])

  val mockUntrainedModel: RandomForestParameters =
    RandomForestParameters(1, "auto", "gini", 4, 100)

  val featuresValues: Seq[Spread[Double]] = Seq(
    Spread(0.0, 0.0), -0.755 +- 0.01,
    Spread(0.0, 0.0), -0.377 +- 0.01,
    Spread(0.0, 0.0), 1.133 +- 0.01
  )

  def validateResult(result: Scorable): Registration = {
    val castedResult = result.asInstanceOf[TrainedRandomForestClassification]
    castedResult.featureColumns shouldBe Seq("column1", "column0", "column4")
    castedResult.targetColumn shouldBe "column3"
  }

  "UntrainedRandomForestClassification" should {

    val inputRows: Seq[Row] = Seq(
      Row(-2.0, "x", 0.0, 1000.0, 0),
      Row(-2.0, "y", 0.0, 2000.0, 1),
      Row(-2.0, "z", 1.0, 6000.0, 2))

    val inputSchema: StructType = StructType(Seq(
      StructField("column1", DoubleType),
      StructField("column2", StringType),
      StructField("column3", DoubleType),
      StructField("column0", DoubleType),
      StructField("column4", IntegerType,
        metadata = MappingMetadataConverter.mappingToMetadata(CategoriesMapping(Seq("A", "B", "C")))
      )
    ))

    lazy val inputDataFrame = createDataFrame(inputRows, inputSchema)

    "create model trained on given dataframe" in {
      val mockContext: ExecutionContext = mock[ExecutionContext]

      val classification = constructUntrainedModel
      val parameters = Trainable.Parameters(
        featureColumns = Some(MultipleColumnSelection(
          Vector(NameColumnSelection(Set("column0", "column1", "column4"))))),
        targetColumn = Some(NameSingleColumnSelection("column3")))

      val result = classification.train(mockContext)(parameters)(inputDataFrame)
      validateResult(result)
    }

    "throw an exception" when {
      "non-existing column was selected as target" in {
        intercept[ColumnDoesNotExistException] {
          val classification = constructUntrainedModel
          val parameters = Trainable.Parameters(
            featureColumns = Some(MultipleColumnSelection(
              Vector(NameColumnSelection(Set("column0", "column1"))))),
            targetColumn = Some(NameSingleColumnSelection("not exists")))
          classification.train(executionContext)(parameters)(inputDataFrame)
        }
        ()
      }
      "non-existing columns were selected as features" in {
        intercept[ColumnsDoNotExistException] {
          val classification = constructUntrainedModel
          val parameters = Trainable.Parameters(
            featureColumns = Some(MultipleColumnSelection(
              Vector(NameColumnSelection(Set("not exists", "column1"))))),
            targetColumn = Some(NameSingleColumnSelection("column3")))
          classification.train(executionContext)(parameters)(inputDataFrame)
        }
        ()
      }
      "some selected features were neither numeric nor categorical" in {
        intercept[WrongColumnTypeException] {
          val classification = constructUntrainedModel
          val parameters = Trainable.Parameters(
            featureColumns = Some(MultipleColumnSelection(
              Vector(NameColumnSelection(Set("column2", "column1"))))),
            targetColumn = Some(NameSingleColumnSelection("column3")))
          classification.train(executionContext)(parameters)(inputDataFrame)
        }
        ()
      }
      "selected target was not numerical" in {
        intercept[WrongColumnTypeException] {
          val classification = constructUntrainedModel
          val parameters = Trainable.Parameters(
            featureColumns = Some(MultipleColumnSelection(
              Vector(NameColumnSelection(Set("column0", "column1"))))),
            targetColumn = Some(NameSingleColumnSelection("column2")))
          classification.train(executionContext)(parameters)(inputDataFrame)
        }
        ()
      }
    }
  }
}
