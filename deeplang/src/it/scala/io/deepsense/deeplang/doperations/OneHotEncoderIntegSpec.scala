/**
 * Copyright (c) 2015, CodiLime Inc.
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

@Ignore
class OneHotEncoderIntegSpec extends DeeplangIntegTestSupport {

  val rows: Seq[Row] = Seq(
    Row("x_abc", "x", "abc"),
    Row("y_abc", "y", "abc"),
    Row("z_abc", "z", "abc"),
    Row("x_ABC", "x", "ABC"),
    Row("y_ABC", "y", "ABC"),
    Row("z_ABC", "z", null)
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

  "OneHotEncoder" should {
    "encode categorical columns" when {
      "one column is selected" in {
        val resultDataFrame = executeOneHotEncoder(
          Set(columns(2)), withRedundancy = false, categorizedDataFrame)
        val expectedDataFrame = enrichedDataFrame(
          Seq(columns(2) + "_ABC"), Seq(Seq(0), Seq(0), Seq(0), Seq(1), Seq(1), Seq(null))
        )
        assertDataFramesEqual(resultDataFrame, expectedDataFrame)
      }
      "two columns are selected" in {
        val resultDataFrame = executeOneHotEncoder(
          Set(columns(1), columns(2)), withRedundancy = false, categorizedDataFrame)
        val expectedDataFrame = enrichedDataFrame(
          Seq(columns(1) + "_x", columns(1) + "_y", columns(2) + "_ABC"),
          Seq(
            Seq(1, 0, 0),
            Seq(0, 1, 0),
            Seq(0, 0, 0),
            Seq(1, 0, 1),
            Seq(0, 1, 1),
            Seq(0, 0, null)
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
          Seq(columns(2) + "_ABC", columns(2) + "_abc"),
          Seq(
            Seq(0, 1),
            Seq(0, 1),
            Seq(0, 1),
            Seq(1, 0),
            Seq(1, 0),
            Seq(null, null)
          )
        )
        assertDataFramesEqual(resultDataFrame, expectedDataFrame)
      }
    }
    "name output columns properly" when {
      "default extensions are occupied" in {
        val dataFrameWithSpecialName = enrichedDataFrame(
          Seq(columns(2) + "_ABC"), Seq.fill(6)(Seq(1)))
        val resultDataFrame = executeOneHotEncoder(
          Set(columns(2)), withRedundancy = false, dataFrameWithSpecialName)
        val expectedDataFrame = enrichedDataFrame(
          Seq(columns(2) + "_ABC_1"), Seq(Seq(0), Seq(0), Seq(0), Seq(1), Seq(1), Seq(null)))
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
    val operation = OneHotEncoder()
    operation.parameters.getColumnSelectorParameter(operation.selectedColumnsKey).value =
      Some(MultipleColumnSelection(Vector(NameColumnSelection(selectedColumns))))
    operation.parameters.getBooleanParameter(operation.withRedundantKey).value =
      Some(withRedundancy)
    operation.execute(executionContext)(Vector(dataFrame)).head.asInstanceOf[DataFrame]
  }

  private def enrichedDataFrame(columnNames: Seq[String], columns: Seq[Seq[Double]]) = {
    createDataFrame(enrichedRows(columns), enrichedSchema(columnNames))
  }

  private def enrichedSchema(additionalColumnNames: Seq[String]): StructType = {
    StructType(schema.fields ++ additionalColumnNames.map(StructField(_, DoubleType)))
  }

  private def enrichedRows(additionalColumns: Seq[Seq[Double]]): Seq[Row] = {
    rows.zip(additionalColumns).map{ case (row, addition) => Row.fromSeq(row.toSeq ++ addition) }
  }
}
