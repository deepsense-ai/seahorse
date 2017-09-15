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

package io.deepsense.deeplang.doperations

import org.apache.spark.sql.Row
import org.apache.spark.sql.types.{DoubleType, StringType, StructField, StructType}
import org.scalatest.Ignore

import io.deepsense.deeplang.DeeplangIntegTestSupport
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.doperables.dataframe.types.categorical.CategoricalMapper
import io.deepsense.deeplang.doperations.exceptions.{ColumnsDoNotExistException, WrongColumnTypeException}
import io.deepsense.deeplang.parameters.{NameColumnSelection, MultipleColumnSelection}

class OneHotEncoderIntegSpec extends DeeplangIntegTestSupport {

  val rows: Seq[Row] = Seq(
    Row("x_abc", "x,x", "abc"),
    Row("y_abc", "y.y", "abc"),
    Row("z_abc", "z:z", "abc"),
    Row("x_ABC", "x,x", "ABC"),
    Row("y_ABC", "y.y", "ABC"),
    Row("z_ABC", "z:z", null)
  )
  val columns = Seq("first_column", "category1", "category2")
  val schema = StructType(List(
    StructField(columns(0), StringType),
    StructField(columns(1), StringType),
    StructField(columns(2), StringType)
  ))
  lazy val dataFrame = createDataFrame(rows, schema)
  lazy val categorizedDataFrame = CategoricalMapper(dataFrame, executionContext.dataFrameBuilder)
    .categorized(columns(1), columns(2))

  val prefix = "prefix"

  "OneHotEncoder" should {
    "encode categorical columns" when {
      "one column is selected" in {
        val resultDataFrame = executeOneHotEncoder(
          Set(columns(2)), withRedundancy = false, categorizedDataFrame)
        val expectedDataFrame = enrichedDataFrame(
          categorizedDataFrame,
          Seq(prefix + columns(2) + "_ABC"),
          Seq(Seq(0.0), Seq(0.0), Seq(0.0), Seq(1.0), Seq(1.0), Seq(null))
        )
        assertDataFramesEqual(resultDataFrame, expectedDataFrame)
      }
      "two columns are selected" in {
        val resultDataFrame = executeOneHotEncoder(
          Set(columns(1), columns(2)), withRedundancy = false, categorizedDataFrame)
        val expectedDataFrame = enrichedDataFrame(
          categorizedDataFrame,
          Seq(
            prefix + columns(1) + "_x,x",
            prefix + columns(1) + "_y_y",
            prefix + columns(2) + "_ABC"),
          Seq(
            Seq(1.0, 0.0, 0.0),
            Seq(0.0, 1.0, 0.0),
            Seq(0.0, 0.0, 0.0),
            Seq(1.0, 0.0, 1.0),
            Seq(0.0, 1.0, 1.0),
            Seq(0.0, 0.0, null)
          )
        )
        assertDataFramesEqual(resultDataFrame, expectedDataFrame)
      }
    }
    "leave redundant column in dataframe" when {
      "is ordered to do so through appropriate parameter" in {
        val resultDataFrame = executeOneHotEncoder(
          Set(columns(2)), withRedundancy = true, categorizedDataFrame)
        val expectedDataFrame = enrichedDataFrame(
          categorizedDataFrame,
          Seq(prefix + columns(2) + "_ABC", prefix + columns(2) + "_abc"),
          Seq(
            Seq(0.0, 1.0),
            Seq(0.0, 1.0),
            Seq(0.0, 1.0),
            Seq(1.0, 0.0),
            Seq(1.0, 0.0),
            Seq(null, null)
          )
        )
        assertDataFramesEqual(resultDataFrame, expectedDataFrame)
      }
    }
    "throw exception" when {
      "column that is not categorical is selected" in {
        an [WrongColumnTypeException] should be thrownBy executeOneHotEncoder(
            Set(columns(0)), withRedundancy = false, categorizedDataFrame)
      }
      "selected columns does not exist" in {
        an [ColumnsDoNotExistException] should be thrownBy executeOneHotEncoder(
            Set(columns(2), "non-existing"), withRedundancy = false, categorizedDataFrame)
      }
    }
  }

  private def executeOneHotEncoder(
      selectedColumns: Set[String],
      withRedundancy: Boolean,
      dataFrame: DataFrame): DataFrame = {

    val operation = OneHotEncoder(
      MultipleColumnSelection(Vector(NameColumnSelection(selectedColumns))),
      withRedundancy,
      Some(prefix))
    operation.execute(executionContext)(Vector(dataFrame)).head.asInstanceOf[DataFrame]
  }

  private def enrichedDataFrame(
      dataFrame: DataFrame,
      additionalColumnNames: Seq[String],
      additionalColumns: Seq[Seq[Any]]) = {

    val sparkDataFrame = dataFrame.sparkDataFrame
    val enrichedRows = sparkDataFrame.collect().zip(additionalColumns).map{
      case (row, addition) => Row.fromSeq(row.toSeq ++ addition)
    }
    val enrichedSchema = StructType(sparkDataFrame.schema.fields ++
      additionalColumnNames.map(StructField(_, DoubleType))
    )
    createDataFrame(enrichedRows, enrichedSchema)
  }
}
