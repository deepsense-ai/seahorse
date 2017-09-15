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
import org.apache.spark.sql.types._

import ai.deepsense.deeplang.DeeplangIntegTestSupport
import ai.deepsense.deeplang.doperables.dataframe.DataFrame
import ai.deepsense.deeplang.doperables.multicolumn.MultiColumnParams.SingleOrMultiColumnChoices.SingleColumnChoice
import ai.deepsense.deeplang.doperables.multicolumn.SingleColumnParams.SingleTransformInPlaceChoices.NoInPlaceChoice
import ai.deepsense.deeplang.doperables.spark.wrappers.transformers.TransformerSerialization
import ai.deepsense.deeplang.doperations.exceptions._
import ai.deepsense.deeplang.params.selections.NameSingleColumnSelection

class SqlColumnTransformationIntegSpec
    extends DeeplangIntegTestSupport
    with TransformerSerialization {

  import TransformerSerialization._

  val resultColumn = 3
  val delta = 0.01
  val column0 = "c0"
  val column1 = "c1"
  val column2 = "c2"
  val column3 = "c3"
  val column3needsEscaping = "c.strange name!"

  "SqlColumnTransformation" should {

    "create Transformation that counts ABS properly" in {
      runTest("ABS(x)", column1, column3, Seq(1.0, 1.1, 1.2, 1.3, null))
    }

    "create Transformation that counts POW properly" in {
      runTest("POW(x, 2.0)", column1, column3, Seq(1.0, 1.21, 1.44, 1.69, null))
    }

    "create Transformation that counts SQRT properly" in {
      runTest("SQRT(x)", column2, column3, Seq(0.447, 1.483, null, 2.04, null))
    }

    "create Transformation that counts SIN properly" in {
      runTest("SIN(x)", column1, column3, Seq(0.841, -0.891, 0.932, -0.96, null))
    }

    "create Transformation that counts COS properly" in {
      runTest("COS(x)", column1, column3, Seq(0.540, 0.453, 0.362, 0.267, null))
    }

    "create Transformation that counts TAN properly" in {
      runTest("TAN(x)", column1, column3, Seq(1.557, -1.964, 2.572, -3.602, null))
    }

    "create Transformation that counts LN properly" in {
      runTest("LN(x)", column2, column3, Seq(-1.609, 0.788, null, 1.435, null))
    }

    "create Transformation that counts FLOOR properly" in {
      runTest("FLOOR(x)", column1, column3, Seq(1.0, -2.0, 1.0, -2.0, null))
    }

    "create Transformation that counts CEIL properly" in {
      runTest("CEIL(x)", column1, column3, Seq(1.0, -1.0, 2.0, -1.0, null))
    }

    "create Transformation that counts SIGNUM properly" in {
      runTest("SIGNUM(x)", column1, column3, Seq(1.0, -1.0, 1.0, -1.0, null))
    }
  }

  it should {
    "create Transformation that counts MINIMUM properly" in {
      runTest(s"MINIMUM($column1, $column2)", column1, column3, Seq(0.2, -1.1, null, -1.3, null))
    }

    "create Transformation that counts MAXIMUM properly" in {
      runTest(s"MAXIMUM($column1, $column2)", column1, column3, Seq(1.0, 2.2, null, 4.2, null))
    }

    "create Transformation that counts complex formulas properly" in {
      runTest(s"MAXIMUM(SIN($column2) + 1.0, ABS($column1 - 2.0))", column1, column3,
        Seq(1.19, 3.1, null, 3.3, null))
    }
  }

  it should {
    "detect missing inputColumn during inference" in {
      a[ColumnDoesNotExistException] shouldBe thrownBy {
        val inPlace = NoInPlaceChoice()
          .setOutputColumn(column3)
        val single = SingleColumnChoice()
          .setInputColumn(NameSingleColumnSelection("nonExistingCol"))
          .setInPlace(inPlace)
        SqlColumnTransformer()
          .setFormula("x * 2")
          .setSingleOrMultiChoice(single)
          ._transformSchema(schema)
      }
    }
    val inPlace = NoInPlaceChoice()
      .setOutputColumn(column3)
    val single = SingleColumnChoice()
      .setInputColumn(NameSingleColumnSelection(column1))
      .setInPlace(inPlace)
    "detect SQL syntax error during inference" in {
      a[SqlColumnExpressionSyntaxException] shouldBe thrownBy(
        SqlColumnTransformer()
          .setFormula("+++---")
          .setSingleOrMultiChoice(single)
          ._transformSchema(schema))
    }
    "detect non-existent column during inference" in {
      a[ColumnsDoNotExistException] shouldBe thrownBy(
        SqlColumnTransformer()
          .setFormula("nonExistingCol")
          .setSingleOrMultiChoice(single)
          ._transformSchema(schema)
      )
    }
    "detect that alias conflicts with a column name form input DF" in {
      a[ColumnAliasNotUniqueException] shouldBe thrownBy(
        SqlColumnTransformer()
          .setFormula("c0")
          .setInputColumnAlias("c0")
          .setSingleOrMultiChoice(single)
          ._transformSchema(schema))
    }
  }

  it should {
    "work with user-defined column alias" in {
      runTest("ABS(y)", column1, column3, Seq(1.0, 1.1, 1.2, 1.3, null), "y")
    }

    "create Transformation that produces properly escaped column name" in {
      val dataFrame = applyFormulaToDataFrame(
        "COS(x)",
        column1,
        s"$column3needsEscaping",
        "x",
        prepareDataFrame())
      val rows = dataFrame.sparkDataFrame.collect()
      validateColumn(rows, Seq(0.540, 0.453, 0.362, 0.267, null))
      val schema = dataFrame.sparkDataFrame.schema
      schema.fieldNames shouldBe Array(column0, column1, column2, column3needsEscaping)
    }

    // TODO: This is Spark bug. Test will be pending till SPARK-13297 is fixed.
    "create Transformation that works on DataFrame with backtics in column names" is pending

    // Regression test
    "create Transformation that works on DataFrame with non-standard column names" in {
      val column0 = "c 0"
      val column1 = "c-2"
      val column2 = "c - 2"
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
      val dataFrame = applyFormulaToDataFrame(
        "COS(x)",
        column1,
        s"$column3needsEscaping",
        "x",
        prepareDataFrame())
      val rows = dataFrame.sparkDataFrame.collect()
      validateColumn(rows, Seq(0.540, 0.453, 0.362, 0.267, null))
      val schema = dataFrame.sparkDataFrame.schema
      schema.fieldNames shouldBe Array(column0, column1, column2, column3needsEscaping)
    }

    "fail when 2 comma-separated formulas are provided" in {
      intercept[SqlColumnExpressionSyntaxException] {
        val dataFrame = applyFormulaToDataFrame(
          "MAXIMUM(x), SIN(x)",
          column1,
          "name",
          "x",
          prepareDataFrame())
        dataFrame.sparkDataFrame.collect()
      };()
    }

    "fail when formula is not correct" in {
      intercept[DOperationExecutionException] {
        val dataFrame =
          applyFormulaToDataFrame("MAXIMUM(", "name", column1, "x", prepareDataFrame())
        dataFrame.sparkDataFrame.collect()
      };()
    }

    "produce NaN if the argument given to the function is not correct" in {
      // counting LN from negative number
      val dataFrame =
        applyFormulaToDataFrame("LN(x)", column1, column3, "x", prepareDataFrame())
      val rowWithNegativeValue = 1
      val rowWithNaN = dataFrame.sparkDataFrame.collect()(rowWithNegativeValue)
      rowWithNaN.getDouble(resultColumn).isNaN shouldBe true
    }

    "always create nullable columns" in {
      runTest("cast(1.0 as double)", column1, column3, Seq(1.0, 1.0, 1.0, 1.0, 1.0))
    }
  }

  def runTest(
      formula: String,
      inputColumnName: String,
      outputColumnName: String,
      expectedValues: Seq[Any],
      columnAlias: String = "x") : Unit = {
    val dataFrame = applyFormulaToDataFrame(
      formula,
      inputColumnName,
      outputColumnName,
      columnAlias,
      prepareDataFrame())
    val rows = dataFrame.sparkDataFrame.collect()
    validateSchema(dataFrame.sparkDataFrame.schema)
    validateColumn(rows, expectedValues)
  }

  def applyFormulaToDataFrame(
      formula: String,
      inputColumnName: String,
      outputColumnName: String,
      columnAlias: String,
      df: DataFrame): DataFrame = {
    val transformation =
      prepareTransformation(formula, inputColumnName, outputColumnName, columnAlias)
    applyTransformation(transformation, df)
  }

  def applyTransformation(transformation: SqlColumnTransformer, df: DataFrame): DataFrame = {
    transformation.applyTransformationAndSerialization(tempDir, df)
  }

  def prepareTransformation(
      formula: String,
      inputColumnName: String,
      outputColumnName: String,
      columnAlias: String): SqlColumnTransformer = {
    val inPlace = NoInPlaceChoice()
      .setOutputColumn(outputColumnName)
    val single = SingleColumnChoice()
      .setInputColumn(NameSingleColumnSelection(inputColumnName))
      .setInPlace(inPlace)
    SqlColumnTransformer()
      .setFormula(formula)
      .setSingleOrMultiChoice(single)
      .setInputColumnAlias(columnAlias)
  }

  def validateSchema(schema: StructType): Unit = {
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
      case (expectedVal, i) =>
        val value = rows(i).get(column)
        value match {
          case d: Double => d should equal (expectedVal.asInstanceOf[Double] +- delta)
          case _ => expectedVal shouldBe value
        }
    }
  }

  def prepareDataFrame(): DataFrame = {
    val manualRowsSeq: Seq[Row] = Seq(
      Row("aaa", 1.0, 0.2),
      Row("bbb", -1.1, 2.2),
      Row("ccc", 1.2, null),
      Row("ddd", -1.3, 4.2),
      Row("eee", null, null))
    createDataFrame(manualRowsSeq, schema)
  }

  val schema: StructType = StructType(List(
    StructField(column0, StringType),
    StructField(column1, DoubleType),
    StructField(column2, DoubleType)))
}
