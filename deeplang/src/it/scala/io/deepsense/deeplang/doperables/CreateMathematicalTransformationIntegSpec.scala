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

import org.apache.spark.sql.Row
import org.apache.spark.sql.types._

import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.doperables.dataframe.types.categorical.{CategoricalMapper, CategoricalMetadata}
import io.deepsense.deeplang.doperations.exceptions.DOperationExecutionException
import io.deepsense.deeplang.doperations.{ApplyTransformation, CreateMathematicalTransformation}
import io.deepsense.deeplang.{DOperable, DeeplangIntegTestSupport}

class CreateMathematicalTransformationIntegSpec extends DeeplangIntegTestSupport {

  val resultColumn = 3
  val delta = 0.01
  val column0 = "c0"
  val column1 = "c1"
  val column2 = "c2"
  val column3 = "c3"
  val column3needsEscaping = "c.strange name!"

  "CreateMathematicalTransformation" should {

    "create Transformation that counts ABS properly" in {
      runTest(s"ABS($column1)", column3, Seq(1.0, 1.1, 1.2, 1.3, null))
    }

    "create Transformation that counts POW properly" in {
      runTest(s"POW($column1, 2.0)", column3, Seq(1.0, 1.21, 1.44, 1.69, null))
    }

    "create Transformation that counts SQRT properly" in {
      runTest(s"SQRT($column2)", column3, Seq(0.447, 1.483, null, 2.04, null))
    }

    "create Transformation that counts SIN properly" in {
      runTest(s"SIN($column1)", column3, Seq(0.841, -0.891, 0.932, -0.96, null))
    }

    "create Transformation that counts COS properly" in {
      runTest(s"COS($column1)", column3, Seq(0.540, 0.453, 0.362, 0.267, null))
    }

    "create Transformation that counts TAN properly" in {
      runTest(s"TAN($column1)", column3, Seq(1.557, -1.964, 2.572, -3.602, null))
    }

    "create Transformation that counts LN properly" in {
      runTest(s"LN($column2)", column3, Seq(-1.609, 0.788, null, 1.435, null))
    }

    "create Transformation that counts MINIMUM properly" in {
      runTest(s"MINIMUM($column1, $column2)", column3, Seq(0.2, -1.1, null, -1.3, null))
    }

    "create Transformation that counts MAXIMUM properly" in {
      runTest(s"MAXIMUM($column1, $column2)", column3, Seq(1.0, 2.2, null, 4.2, null))
    }

    "create Transformation that counts FLOOR properly" in {
      runTest(s"FLOOR($column1)", column3, Seq(1.0, -2.0, 1.0, -2.0, null))
    }

    "create Transformation that counts CEIL properly" in {
      runTest(s"CEIL($column1)", column3, Seq(1.0, -1.0, 2.0, -1.0, null))
    }

    "create Transformation that counts SIGNUM properly" in {
      runTest(s"SIGNUM($column1)", column3, Seq(1.0, -1.0, 1.0, -1.0, null))
    }

    "create Transformation that counts complex formulas properly" in {
      runTest(s"MAXIMUM(SIN($column2) + 1.0, ABS($column1 - 2.0))", column3,
        Seq(1.19, 3.1, null, 3.3, null))
    }

    "create Transformation that produces properly escaped column name" in {
      val dataFrame = applyFormulaToDataFrame(
        s"COS($column1)", s"$column3needsEscaping",
        prepareDataFrame())
      val rows = dataFrame.sparkDataFrame.collect()
      validateColumn(rows, Seq(0.540, 0.453, 0.362, 0.267, null))
      val schema = dataFrame.sparkDataFrame.schema
      schema.fieldNames shouldBe Array(column0, column1, column2, column3needsEscaping)
    }

    "fail when 2 comma-separated formulas are provided" in {
      intercept[DOperationExecutionException] {
        val dataFrame = applyFormulaToDataFrame(
          s"MAXIMUM($column1), SIN($column1)", "name",
          prepareDataFrame())
        dataFrame.sparkDataFrame.collect()
      };()
    }

    "fail when formula is not correct" in {
      intercept[DOperationExecutionException] {
        val dataFrame = applyFormulaToDataFrame("MAXIMUM(", "name", prepareDataFrame())
        dataFrame.sparkDataFrame.collect()
      };()
    }

    "produce NaN if the argument given to the function is not correct" in {
      // counting LN from negative number
      val dataFrame = applyFormulaToDataFrame(s"LN($column1)", column3, prepareDataFrame())
      val rowWithNegativeValue = 1
      val rowWithNaN = dataFrame.sparkDataFrame.collect()(rowWithNegativeValue)
      rowWithNaN.getDouble(resultColumn).isNaN shouldBe true
    }

    "retain the categorical column type" in {
      val dataFrame = applyFormulaToDataFrame(s"SIN($column1)", column3,
        prepareDataFrameWithCategorical())
      CategoricalMetadata(dataFrame).isCategorical(column0) shouldBe true
      CategoricalMetadata(dataFrame).isCategorical(column1) shouldBe false
      CategoricalMetadata(dataFrame).isCategorical(column2) shouldBe false
      CategoricalMetadata(dataFrame).isCategorical(column3) shouldBe false
    }

    "always create nullable columns" in {
      runTest(s"1.0", column3, Seq(1.0, 1.0, 1.0, 1.0, 1.0))
    }
  }

  def runTest(formula: String, columnName: String, expectedValues: Seq[Any]) : Unit = {
    val dataFrame = applyFormulaToDataFrame(formula, columnName, prepareDataFrame())
    val rows = dataFrame.sparkDataFrame.collect()
    validateSchema(dataFrame.sparkDataFrame.schema)
    validateColumn(rows, expectedValues)
  }

  def applyFormulaToDataFrame(formula: String, columnName: String, df: DataFrame): DataFrame = {
    val transformation = prepareTransformation(formula, columnName)
    applyTransformation(transformation, df)
  }

  def applyTransformation(transformation: Transformation, df: DataFrame): DataFrame = {
    new ApplyTransformation()
      .execute(executionContext)(Vector(transformation, df))
      .head.asInstanceOf[DataFrame]
  }

  def prepareTransformation(formula: String, columnName: String): Transformation = {
    val createMathematicalTransformation = new CreateMathematicalTransformation()
    createMathematicalTransformation.formulaParam.value = Some(formula)
    createMathematicalTransformation.columnNameParam.value = Some(columnName)
    createMathematicalTransformation.execute(executionContext)(
      Vector.empty[DOperable]
    ).head.asInstanceOf[Transformation]
  }

  def validateSchema(schema: StructType) = {
    schema.fieldNames shouldBe Array(column0, column1, column2, column3)
    schema.fields(0).dataType shouldBe StringType
    schema.fields(0) shouldBe 'nullable
    schema.fields(1).dataType shouldBe DoubleType
    schema.fields(1) shouldBe 'nullable
    schema.fields(2).dataType shouldBe DoubleType
    schema.fields(3) shouldBe 'nullable
    schema.fields(3).dataType shouldBe DoubleType
    schema.fields(3) shouldBe 'nullable
  }

  /**
   * Check if produced column matches the expected values
   */
  def validateColumn(
    rows: Array[Row], expectedValues: Seq[Any], column: Integer = resultColumn): Unit = {
    forAll(expectedValues.zipWithIndex) {
      case (expectedVal, i) => {
        val value = rows(i).get(column)
        value match {
          case d: Double => d should equal (expectedVal.asInstanceOf[Double] +- delta)
          case _ => expectedVal shouldBe value
        }
      }
    }
  }

  def prepareDataFrameWithCategorical(): DataFrame = {
    val df = prepareDataFrame()
    CategoricalMapper(df, executionContext.dataFrameBuilder).categorized(column0)
  }

  def prepareDataFrame(): DataFrame = {
    val schema: StructType = StructType(List(
      StructField(column0, StringType),
      StructField(column1, DoubleType),
      StructField(column2, DoubleType)))
    val manualRowsSeq: Seq[Row] = Seq(
      Row("aaa", 1.0, 0.2),
      Row("bbb", -1.1, 2.2),
      Row("ccc", 1.2, null),
      Row("ddd", -1.3, 4.2),
      Row("eee", null, null))
    createDataFrame(manualRowsSeq, schema)
  }
}
