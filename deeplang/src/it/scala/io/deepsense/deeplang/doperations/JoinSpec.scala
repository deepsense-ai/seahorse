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

import org.apache.spark.sql.Row
import org.apache.spark.sql.types._

import io.deepsense.deeplang._
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.doperables.dataframe.types.categorical.{CategoricalMapper, CategoricalMetadata}
import io.deepsense.deeplang.doperations.exceptions.{ColumnDoesNotExistException, ColumnsDoNotExistException, WrongColumnTypeException}
import io.deepsense.deeplang.parameters._

class JoinSpec extends DeeplangIntegTestSupport {

  val leftTablePrefix = Some("leftTable_")
  val rightTablePrefix = Some("rightTable_")

  "Join operation" should {
    "LEFT JOIN two DataFrames" when {
      "based upon a single column selection by name" in {
        val (ldf, rdf, expected, joinColumns) = oneColumnFixture()

        val join = joinWithMultipleColumnSelection(joinColumns, Set.empty)
        val joinDF = executeOperation(join, ldf, rdf)

        assertDataFramesEqual(joinDF, expected, checkRowOrder = false)
      }
      "based upon a single column selection by index" in {
        val (ldf, rdf, expected, joinColumnIds) = oneColumnByIndicesFixture()

        val join = joinWithMultipleColumnSelection(Set.empty, joinColumnIds)
        val joinDF = executeOperation(join, ldf, rdf)

        assertDataFramesEqual(joinDF, expected, checkRowOrder = false)
      }
      "based upon a single column selection with colliding join column" in {
        val (ldf, rdf, expected, joinColumns) = oneColumnFixture(None, None)

        val join = joinWithMultipleColumnSelection(
          joinColumns, Set.empty, leftPrefix = None, rightPrefix = None)

        val joinDF = executeOperation(join, ldf, rdf)

        assertDataFramesEqual(joinDF, expected, checkRowOrder = false)
      }
      "based upon two columns" in {
        val (ldf, rdf, expected, joinColumns) = twoColumnsFixture()

        val join = joinWithMultipleColumnSelection(joinColumns, Set.empty)
        val joinDF = executeOperation(join, ldf, rdf)

        assertDataFramesEqual(joinDF, expected, checkRowOrder = false)
      }
      "based upon two columns with different column names" in {
        val (ldf, rdf, expected, leftJoinColumns, rightJoinColumns) =
          twoColumnsDifferentNamesFixture()

        val join = joinWithMultipleColumnSelection(
          leftJoinColumns,
          Vector.empty,
          rightJoinColumns,
          Vector.empty,
          leftPrefix = None,
          rightPrefix = None)

        val joinDF = executeOperation(join, ldf, rdf)

        assertDataFramesEqual(joinDF, expected, checkRowOrder = false)
      }
      "based upon two columns with different column indices" in {
        val (ldf, rdf, expected, leftJoinIndices, rightJoinIndices) =
          twoColumnsDifferentIndicesFixture()

        val join = joinWithMultipleColumnSelection(
          Vector.empty,
          leftJoinIndices,
          Vector.empty,
          rightJoinIndices,
          leftPrefix = None,
          rightPrefix = None)

        val joinDF = executeOperation(join, ldf, rdf)

        assertDataFramesEqual(joinDF, expected, checkRowOrder = false)
      }
      "some rows from left dataframe have no corresponding values in the right one" in {
        val (ldf, rdf, expected, joinColumns) = noSomeRightValuesFixture()

        val join = joinWithMultipleColumnSelection(joinColumns, Set.empty)
        val joinDF = executeOperation(join, ldf, rdf)

        assertDataFramesEqual(joinDF, expected, checkRowOrder = false)
      }
      "dataframes have no matching values" in {
        val (ldf, rdf, expected, joinColumns) = noMatchingValuesFixture()

        val join = joinWithMultipleColumnSelection(joinColumns, Set.empty)
        val joinDF = executeOperation(join, ldf, rdf)

        assertDataFramesEqual(joinDF, expected, checkRowOrder = false)
      }
      "some column values are null" in {
        val (ldf, rdf, expected, joinColumns) = nullFixture()

        val join = joinWithMultipleColumnSelection(joinColumns, Set.empty)
        val joinDF = executeOperation(join, ldf, rdf)

        assertDataFramesEqual(joinDF, expected, checkRowOrder = false)
      }
      "using categorical column" in {
        val (ldf, rdf, expected, joinColumns) = categoricalFixture()

        val join = joinWithMultipleColumnSelection(joinColumns, Set.empty)
        val joinDF = executeOperation(join, ldf, rdf)

        assertDataFramesEqual(joinDF, expected, checkRowOrder = false)
      }
      "with null values only in left DataFrame" in {
        val (ldf, rdf, expected, joinColumns) = nullValuesInLeftDataFrameFixture()

        val join = joinWithMultipleColumnSelection(joinColumns, Set.empty)
        val joinDF = executeOperation(join, ldf, rdf)

        assertDataFramesEqual(joinDF, expected, checkRowOrder = false)
      }
      "with null values only in both DataFrames" is pending
      "with empty join column selection" is pending
    }
    "throw an exception" when {
      "with columns of the same name in both and no join on them" in {
        an[Exception] should be thrownBy {
          val (ldf, rdf, expected, joinColumns) = sameColumnNamesFixture()

          val join = joinWithMultipleColumnSelection(joinColumns, Set.empty)
          val joinDF = executeOperation(join, ldf, rdf)

          assertDataFramesEqual(joinDF, expected, checkRowOrder = false)
        }
      }
      "the columns selected by name does not exist" in {
        an[ColumnDoesNotExistException] should be thrownBy {
          val nonExistingColumnName = "thisColumnDoesNotExist"
          val join = joinWithMultipleColumnSelection(
            Set(nonExistingColumnName),
            Set.empty
          )
          val (ldf, rdf, _, _) = oneColumnFixture()
          executeOperation(join, ldf, rdf)
        }
      }
      "the columns selected by index does not exist" in {
        an[ColumnDoesNotExistException] should be thrownBy {
          val nonExistingColumnIndex = 1000
          val join = joinWithMultipleColumnSelection(
            Set.empty,
            Set(nonExistingColumnIndex)
          )
          val (ldf, rdf, _, _) = oneColumnFixture()
          executeOperation(join, ldf, rdf)
        }
      }
      "the columns selected by name are of different types" in {
        an[WrongColumnTypeException] should be thrownBy {
          val (ldf, rdf, _, wrongTypeColumnNames) = differentTypesFixture()
          val join = joinWithMultipleColumnSelection(
            wrongTypeColumnNames,
            Set.empty
          )
          executeOperation(join, ldf, rdf)
        }
      }
      "the joinColumns MultipleColumnSelector is empty" in {
        an[ColumnsDoNotExistException] should be thrownBy {
          val (ldf, rdf, _, wrongTypeColumnNames) = joinColumnsIsEmptyFixture()
          val join = joinWithMultipleColumnSelection(
            wrongTypeColumnNames,
            Set.empty
          )
          executeOperation(join, ldf, rdf)
        }
      }
    }
  }

  def oneColumnFixture(
      leftPrefix: Option[String] = leftTablePrefix,
      rightPrefix: Option[String] = rightTablePrefix)
      : (DataFrame, DataFrame, DataFrame, Set[String]) = {
    val column1 = "column1"
    val joinColumns = Set(column1)

    // Left dataframe
    val colsL = Vector("column2", "column3", column1, "column4")
    val schemaL = StructType(Seq(
      StructField(colsL(0), DoubleType),
      StructField(colsL(1), StringType),
      StructField(colsL(2), DoubleType),
      StructField(colsL(3), DoubleType)
    ))
    val rowsL = Seq(
      (3.5, "a", 1.5, 5.0),
      (3.6, "b", 1.6, 6.0),
      (3.7, "c", 1.7, 10.0),
      (4.6, "d", 1.6, 9.0),
      (4.5, "e", 1.5, 11.0)
    ).map(Row.fromTuple)
    val ldf = createDataFrame(rowsL, schemaL)

    // Right dataframe
    val colsR = Vector(column1, "column22", "column5")
    val schemaR = StructType(Seq(
      StructField(colsR(0), DoubleType),
      StructField(colsR(1), DoubleType),
      StructField(colsR(2), StringType)
    ))
    val rowsR = Seq(
      (1.6, 2.6, "two"),
      (1.7, 2.7, "three"),
      (1.5, 2.5, "one"),
      (1.5, 3.5, "four")
    ).map(Row.fromTuple)
    val rdf = createDataFrame(rowsR, schemaR)

    // join dataframe
    val joinRows = Seq(
      (3.5, "a", 1.5, 5, 2.5, "one"),
      (3.5, "a", 1.5, 5, 3.5, "four"),
      (3.6, "b", 1.6, 6, 2.6, "two"),
      (3.7, "c", 1.7, 10, 2.7, "three"),
      (4.6, "d", 1.6, 9, 2.6, "two"),
      (4.5, "e", 1.5, 11, 2.5, "one"),
      (4.5, "e", 1.5, 11, 3.5, "four")
    ).map(Row.fromTuple)
    val joinSchema = StructType(
      appendPrefix(schemaL.fields, leftPrefix) ++
      appendPrefix(Seq(
          StructField(colsR(1), DoubleType),
          StructField(colsR(2), StringType)),
        rightPrefix)
    )
    val edf = createDataFrame(joinRows, joinSchema)

    (ldf, rdf, edf, joinColumns)
  }

  def oneColumnByIndicesFixture(
                        leftPrefix: Option[String] = leftTablePrefix,
                        rightPrefix: Option[String] = rightTablePrefix)
  : (DataFrame, DataFrame, DataFrame, Set[Int]) = {
    val column1 = "column1"

    // Left dataframe
    val colsL = Vector(column1, "column2", "column3", "column4")
    val schemaL = StructType(Seq(
      StructField(colsL(0), DoubleType),
      StructField(colsL(1), DoubleType),
      StructField(colsL(2), StringType),
      StructField(colsL(3), DoubleType)
    ))
    val rowsL = Seq(
      (1.5, 3.5, "a", 5.0),
      (1.6, 3.6, "b", 6.0),
      (1.7, 3.7, "c", 10.0),
      (1.6, 4.6, "d", 9.0),
      (1.5, 4.5, "e", 11.0)
    ).map(Row.fromTuple)
    val ldf = createDataFrame(rowsL, schemaL)

    // Right dataframe
    val colsR = Vector(column1, "column22", "column5")
    val schemaR = StructType(Seq(
      StructField(colsR(0), DoubleType),
      StructField(colsR(1), DoubleType),
      StructField(colsR(2), StringType)
    ))
    val rowsR = Seq(
      (1.6, 2.6, "two"),
      (1.7, 2.7, "three"),
      (1.5, 2.5, "one"),
      (1.5, 3.5, "four")
    ).map(Row.fromTuple)
    val rdf = createDataFrame(rowsR, schemaR)

    // join dataframe
    val joinRows = Seq(
      (1.5, 3.5, "a", 5, 2.5, "one"),
      (1.5, 3.5, "a", 5, 3.5, "four"),
      (1.6, 3.6, "b", 6, 2.6, "two"),
      (1.7, 3.7, "c", 10, 2.7, "three"),
      (1.6, 4.6, "d", 9, 2.6, "two"),
      (1.5, 4.5, "e", 11, 2.5, "one"),
      (1.5, 4.5, "e", 11, 3.5, "four")
    ).map(Row.fromTuple)
    val joinSchema = StructType(
      appendPrefix(schemaL.fields, leftPrefix) ++
        appendPrefix(Seq(
          StructField(colsR(1), DoubleType),
          StructField(colsR(2), StringType)),
          rightPrefix)
    )
    val edf = createDataFrame(joinRows, joinSchema)

    (ldf, rdf, edf, Set(0))
  }

  def twoColumnsFixture(): (DataFrame, DataFrame, DataFrame, Set[String]) = {
    val column1 = "column1"
    val column2 = "column2"
    val joinColumns = Set(column1, column2)

    // Left dataframe
    val colsL = Vector(column2, "column3", column1, "column4")
    val schemaL = StructType(Seq(
      StructField(colsL(0), DoubleType),
      StructField(colsL(1), StringType),
      StructField(colsL(2), DoubleType),
      StructField(colsL(3), DoubleType)
    ))
    val rowsL = Seq(
      (2.5, "a", 1.5, 5.0),
      (3.6, "b", 1.6, 6.0),
      (3.7, "c", 1.7, 10.0)
    ).map(Row.fromTuple)
    val ldf = createDataFrame(rowsL, schemaL)

    // Right dataframe
    val colsR = Vector(column1, column2, "column5")
    val schemaR = StructType(Seq(
      StructField(colsR(0), DoubleType),
      StructField(colsR(1), DoubleType),
      StructField(colsR(2), StringType)
    ))
    val rowsR = Seq(
      (1.5, 2.5, "one"),
      (1.5, 3.6, "two"),
      (1.7, 3.6, "c")
    ).map(Row.fromTuple)
    val rdf = createDataFrame(rowsR, schemaR)

    // join dataframe
    val joinRows = Seq(
      (2.5, "a", 1.5, 5, "one"),
      (3.6, "b", 1.6, 6, null),
      (3.7, "c", 1.7, 10, null)
    ).map(Row.fromTuple)
    val joinSchema = StructType(
      appendPrefix(schemaL.fields, leftTablePrefix) ++
      appendPrefix(Seq(StructField(colsR(2), StringType)), rightTablePrefix)
    )
    val edf = createDataFrame(joinRows, joinSchema)

    (ldf, rdf, edf, joinColumns)
  }

  def twoColumnsDifferentNamesFixture(): (
    DataFrame, DataFrame, DataFrame, Vector[String], Vector[String]) = {

    // Left dataframe
    val colsL = Vector("a", "b", "c", "d")
    val schemaL = StructType(Seq(
      StructField(colsL(0), DoubleType),
      StructField(colsL(1), StringType),
      StructField(colsL(2), DoubleType),
      StructField(colsL(3), DoubleType)
    ))
    val rowsL = Seq(
      (2.5, "a", 1.5, 5.0),
      (3.6, "b", 1.6, 6.0),
      (3.7, "c", 1.7, 10.0)
    ).map(Row.fromTuple)
    val ldf = createDataFrame(rowsL, schemaL)

    // Right dataframe
    val colsR = Vector("e", "f", "g")
    val schemaR = StructType(Seq(
      StructField(colsR(0), DoubleType),
      StructField(colsR(1), DoubleType),
      StructField(colsR(2), StringType)
    ))
    val rowsR = Seq(
      (1.5, 2.5, "one"),
      (1.5, 3.6, "two"),
      (1.7, 3.6, "c")
    ).map(Row.fromTuple)
    val rdf = createDataFrame(rowsR, schemaR)

    // join dataframe
    val joinRows = Seq(
      (2.5, "a", 1.5, 5, "one"),
      (3.6, "b", 1.6, 6, null),
      (3.7, "c", 1.7, 10, null)
    ).map(Row.fromTuple)
    val joinSchema = StructType(
      schemaL.fields ++ Seq(StructField(colsR(2), StringType))
    )
    val edf = createDataFrame(joinRows, joinSchema)

    (ldf, rdf, edf, Vector("c", "a"), Vector("e", "f"))
  }

  def twoColumnsDifferentIndicesFixture(): (
    DataFrame, DataFrame, DataFrame, Vector[Int], Vector[Int]) = {

    // Left dataframe
    val colsL = Vector("a", "b", "c", "d")
    val schemaL = StructType(Seq(
      StructField(colsL(0), DoubleType),
      StructField(colsL(1), StringType),
      StructField(colsL(2), DoubleType),
      StructField(colsL(3), DoubleType)
    ))
    val rowsL = Seq(
      (2.5, "a", 1.5, 5.0),
      (3.6, "b", 1.6, 6.0),
      (3.7, "c", 1.7, 10.0)
    ).map(Row.fromTuple)
    val ldf = createDataFrame(rowsL, schemaL)

    // Right dataframe
    val colsR = Vector("e", "f", "g")
    val schemaR = StructType(Seq(
      StructField(colsR(0), DoubleType),
      StructField(colsR(1), DoubleType),
      StructField(colsR(2), StringType)
    ))
    val rowsR = Seq(
      (1.5, 2.5, "one"),
      (1.5, 3.6, "two"),
      (1.7, 3.6, "c")
    ).map(Row.fromTuple)
    val rdf = createDataFrame(rowsR, schemaR)

    // join dataframe
    val joinRows = Seq(
      (2.5, "a", 1.5, 5, "one"),
      (3.6, "b", 1.6, 6, null),
      (3.7, "c", 1.7, 10, null)
    ).map(Row.fromTuple)
    val joinSchema = StructType(
      schemaL.fields ++ Seq(StructField(colsR(2), StringType))
    )
    val edf = createDataFrame(joinRows, joinSchema)

    (ldf, rdf, edf, Vector(2, 0), Vector(0, 1))
  }

  def differentTypesFixture(): (DataFrame, DataFrame, DataFrame, Set[String]) = {
    val column1 = "column1"
    val joinColumns = Set(column1)

    // Left dataframe
    val colsL = Vector(column1, "column2", "column3", "column4")
    val rowsL = Seq(
      (1.5, 2.5, "a", 3.5),
      (1.6, 2.6, "b", 3.6),
      (1.7, 2.7, "c", 3.7)
    ).map(Row.fromTuple)
    val schemaL = StructType(Seq(
      StructField(colsL(0), DoubleType),
      StructField(colsL(1), DoubleType),
      StructField(colsL(2), StringType),
      StructField(colsL(3), DoubleType)
    ))
    val ldf = createDataFrame(rowsL, schemaL)

    // Right dataframe
    val colsR = Vector(column1, "column22")
    val rowsR = Seq(
      ("1.5", 0.5),
      ("1.6", 0.6),
      ("1.7", 0.7)
    ).map(Row.fromTuple)
    val schemaR = StructType(Seq(
      StructField(colsR(0), StringType),
      StructField(colsR(1), DoubleType)
    ))
    val rdf = createDataFrame(rowsR, schemaR)

    val ignored = mock[DataFrame]

    (ldf, rdf, ignored, joinColumns)
  }

  def joinColumnsIsEmptyFixture(): (DataFrame, DataFrame, DataFrame, Set[String]) = {
    val joinColumns = Set.empty[String]

    // Left dataframe
    val colsL = Vector("column1", "column2", "column3", "column4")
    val rowsL = Seq(
      (1.5, 2.5, "a", 3.5),
      (1.6, 2.6, "b", 3.6),
      (1.7, 2.7, "c", 3.7)
    ).map(Row.fromTuple)
    val schemaL = StructType(Seq(
      StructField(colsL(0), DoubleType),
      StructField(colsL(1), DoubleType),
      StructField(colsL(2), StringType),
      StructField(colsL(3), DoubleType)
    ))
    val ldf = createDataFrame(rowsL, schemaL)

    // Right dataframe
    val colsR = Vector("column1", "column22")
    val rowsR = Seq(
      ("1.5", 0.5),
      ("1.6", 0.6),
      ("1.7", 0.7)
    ).map(Row.fromTuple)
    val schemaR = StructType(Seq(
      StructField(colsR(0), StringType),
      StructField(colsR(1), DoubleType)
    ))
    val rdf = createDataFrame(rowsR, schemaR)

    val ignored = mock[DataFrame]

    (ldf, rdf, ignored, joinColumns)
  }

  def noSomeRightValuesFixture(): (DataFrame, DataFrame, DataFrame, Set[String]) = {
    val column1 = "column1"
    val joinColumns = Set(column1)

    // Left dataframe
    val colsL = Vector(column1)
    val rowsL = Seq(
      1.5,
      1.6,
      1.7
    ).map(Seq(_)).map(Row.fromSeq)
    val schemaL = StructType(Seq(
      StructField(colsL(0), DoubleType)
    ))
    val ldf = createDataFrame(rowsL, schemaL)

    // Right dataframe
    val colsR = Vector(column1, "column2")
    val rowsR = Seq(
      (1.4, 0.5),
      (1.7, 2.7)
    ).map(Row.fromTuple)
    val schemaR = StructType(Seq(
      StructField(colsR(0), DoubleType),
      StructField(colsR(1), DoubleType)
    ))
    val rdf = createDataFrame(rowsR, schemaR)

    // join dataframe
    val joinRows = Seq(
      (1.5, null),
      (1.6, null),
      (1.7, 2.7)
    ).map(Row.fromTuple)
    val joinSchema = StructType(
      appendPrefix(schemaL.fields, leftTablePrefix) ++
      appendPrefix(schemaR.fields.filterNot(_.name == column1), rightTablePrefix))
    val edf = createDataFrame(joinRows, joinSchema)

    (ldf, rdf, edf, joinColumns)
  }

  def noMatchingValuesFixture(): (DataFrame, DataFrame, DataFrame, Set[String]) = {
    val column1 = "column1"
    val joinColumns = Set(column1)

    // Left dataframe
    val colsL = Vector(column1)
    val schemaL = StructType(Seq(
      StructField(column1, DoubleType)
    ))
    val rowsL = Seq(
      1.5,
      1.6,
      1.7
    ).map(Seq(_)).map(Row.fromSeq)
    val ldf = createDataFrame(rowsL, schemaL)

    // Right dataframe
    val colsR = Vector(column1, "column2")
    val schemaR = StructType(Seq(
      StructField(colsR(0), DoubleType),
      StructField(colsR(1), DoubleType)
    ))
    val rowsR = Seq(
      (1.4, 0.5),
      (1.8, 1.7)
    ).map(Row.fromTuple)
    val rdf = createDataFrame(rowsR, schemaR)

    // join dataframe
    val joinRows = Seq(
      (1.5, null),
      (1.6, null),
      (1.7, null)
    ).map(Row.fromTuple)
    val joinSchema = StructType(
      appendPrefix(schemaL.fields, leftTablePrefix) ++
      appendPrefix(schemaR.fields.tail, rightTablePrefix))
    val edf = createDataFrame(joinRows, joinSchema)

    (ldf, rdf, edf, joinColumns)
  }

  def nullFixture(): (DataFrame, DataFrame, DataFrame, Set[String]) = {
    val column1 = "column1"
    val joinColumns = Set(column1)

    // Left dataframe
    val colsL = Vector(column1)
    val rowsL = Seq(
      1.5,
      1.6,
      null
    ).map(Seq(_)).map(Row.fromSeq)
    val schemaL = StructType(Seq(
      StructField(colsL(0), DoubleType)
    ))
    val ldf = createDataFrame(rowsL, schemaL)

    // Right dataframe
    val colsR = Vector(column1, "column2")
    val rowsR = Seq(
      (null, 0.5),
      (null, 0.7),
      (1.6, null)
    ).map(Row.fromTuple)
    val schemaR = StructType(Seq(
      StructField(colsR(0), DoubleType),
      StructField(colsR(1), DoubleType)
    ))
    val rdf = createDataFrame(rowsR, schemaR)

    // join dataframe
    val joinRows = Seq(
      (1.5, null),
      (1.6, null),
      (null, null)
    ).map(Row.fromTuple)
    val joinSchema = StructType(
      appendPrefix(schemaL.fields, leftTablePrefix) ++
      appendPrefix(schemaR.fields.filterNot(_.name == column1), rightTablePrefix)
    )
    val edf = createDataFrame(joinRows, joinSchema)

    (ldf, rdf, edf, joinColumns)
  }

  def categoricalFixture(): (DataFrame, DataFrame, DataFrame, Set[String]) = {
    val column1 = "categorical"
    val joinColumns = Set(column1)

    // Left dataframe
    val colsL = Vector(column1, "age")
    val schemaL = StructType(Seq(
      StructField(colsL(0), StringType),
      StructField(colsL(1), DoubleType)
    ))
    val rowsL = Seq(
      ("pies", 3.0),
      ("kot", 5.0),
      ("krowa", 7.0),
      ("pies", 1.0)
    ).map(Row.fromTuple)
    val ldf = createDataFrame(rowsL, schemaL)
    val ldfCategorized =
      CategoricalMapper(ldf, executionContext.dataFrameBuilder).categorized(column1)

    val lcm = CategoricalMetadata(ldfCategorized)
    val lMapping = lcm.mapping(column1)

    // Right dataframe
    val colsR = Vector(column1, "owner")
    val schemaR = StructType(Seq(
      StructField(colsL(0), StringType),
      StructField(colsR(1), StringType)
    ))
    val rowsR = Seq(
      ("kot", "Wojtek"),
      ("wiewiorka", "Jacek"),
      ("pies", "Rafal")
    ).map(Row.fromTuple)
    val rdf = createDataFrame(rowsR, schemaR)
    val rdfCategorized =
      CategoricalMapper(rdf, executionContext.dataFrameBuilder).categorized(column1)

    val rcm = CategoricalMetadata(rdfCategorized)
    val rMapping = rcm.mapping(column1)

    val merged = lMapping.mergeWith(rMapping)
    val finalMapping = merged.finalMapping

    // join dataframe
    val joinRows = Seq(
      (finalMapping.valueToId("pies"), 3.0, "Rafal"),
      (finalMapping.valueToId("kot"), 5.0, "Wojtek"),
      (finalMapping.valueToId("krowa"), 7.0, null),
      (finalMapping.valueToId("pies"), 1.0, "Rafal")
    ).map(Row.fromTuple)
    val joinSchema = StructType(
      appendPrefix(
        Seq(StructField(colsL(0), IntegerType)) ++ schemaL.fields.tail, leftTablePrefix) ++
      appendPrefix(
        schemaR.fields.filterNot(_.name == column1), rightTablePrefix)
    )
    val edf = createDataFrame(joinRows, joinSchema)

    (ldfCategorized, rdfCategorized, edf, joinColumns)
  }

  def nullValuesInLeftDataFrameFixture(): (DataFrame, DataFrame, DataFrame, Set[String]) = {
    val column1 = "nulls"
    val joinColumns = Set(column1)

    // Left dataframe
    val colsL = Vector(column1)
    val schemaL = StructType(Seq(
      StructField(colsL(0), StringType)
    ))
    val rowsL = Seq(
      null
    ).map(Seq(_)).map(Row.fromSeq)
    val ldf = createDataFrame(rowsL, schemaL)
    val ldfCategorized =
      CategoricalMapper(ldf, executionContext.dataFrameBuilder).categorized(column1)

    val lcm = CategoricalMetadata(ldfCategorized)
    val lMapping = lcm.mapping(0)

    // Right dataframe
    val colsR = Vector(column1, "owner")
    val schemaR = StructType(Seq(
      StructField(colsL(0), StringType),
      StructField(colsR(1), StringType)
    ))
    val rowsR = Seq(
      ("kot", "Wojtek"),
      ("wiewiorka", "Jacek"),
      ("pies", "Rafal")
    ).map(Row.fromTuple)
    val rdf = createDataFrame(rowsR, schemaR)
    val rdfCategorized =
      CategoricalMapper(rdf, executionContext.dataFrameBuilder).categorized(column1)

    val rcm = CategoricalMetadata(rdfCategorized)
    val rMapping = rcm.mapping(column1)

    val merged = lMapping.mergeWith(rMapping)
    val finalMapping = merged.finalMapping

    // join dataframe
    val joinRows = Seq(
      (null, null)
    ).map(Row.fromTuple)
    val joinSchema = StructType(
      appendPrefix(
        Seq(StructField(colsL(0), IntegerType)) ++ schemaL.fields.tail, leftTablePrefix) ++
      appendPrefix(
        schemaR.fields.filterNot(_.name == column1), rightTablePrefix)
    )
    val edf = createDataFrame(joinRows, joinSchema)

    (ldfCategorized, rdfCategorized, edf, joinColumns)
  }

  private def sameColumnNamesFixture(): (DataFrame, DataFrame, DataFrame, Set[String]) = {
    val column1 = "nulls"
    val joinColumns = Set(column1)

    val sameNameColumns = Seq(
      ("A", StringType),
      ("B", DoubleType)
    )

    object Gen {
      def generate(dt: DataType) = dt match {
        case StringType => "s"
        case DoubleType => 1.0
      }
    }
    import Gen._

    // Left dataframe
    val colsL = Vector(column1) ++ sameNameColumns.map { case (name, _) => name }
    val schemaL = StructType(Seq(
      StructField(colsL(0), StringType)
    ) ++ sameNameColumns.map { case (name, tpe) => StructField(name, tpe) })
    val rowsL = Seq(
      null +: sameNameColumns.map { case (_, t) => generate(t) }
    ).map(Row.fromSeq)
    val ldf = createDataFrame(rowsL, schemaL)

    // Right dataframe
    val colsR = Vector(column1, "owner") ++ sameNameColumns.map { case (name, _) => name }
    val schemaR = StructType(Seq(
      StructField(colsL(0), StringType),
      StructField(colsR(1), StringType)
    ) ++ sameNameColumns.map { case (name, tpe) => StructField(name, tpe) })
    val rowsR = Seq(
      Seq("kot", "Wojtek") ++ sameNameColumns.map { case (_, t) => generate(t) },
      Seq("wiewiorka", "Jacek") ++ sameNameColumns.map { case (_, t) => generate(t) },
      Seq("pies", "Rafal") ++ sameNameColumns.map { case (_, t) => generate(t) }
    ).map(Row.fromSeq)
    val rdf = createDataFrame(rowsR, schemaR)

    // join dataframe
    val joinRows = Seq(
      (null, "s", 1, null, null, null)
    ).map(Row.fromTuple)
    val joinSchema = StructType(schemaL.fields ++ schemaR.fields)
    val edf = createDataFrame(joinRows, joinSchema)

    (ldf, rdf, edf, joinColumns)
  }

  private def joinWithMultipleColumnSelection(
      namesLeft: Vector[String],
      idsLeft: Vector[Int],
      namesRight: Vector[String],
      idsRight: Vector[Int],
      leftPrefix: Option[String],
      rightPrefix: Option[String]): Join = {
    val operation = new Join

    val paramsByName: Seq[Join.ColumnPair] =
      namesLeft.zip(namesRight).map({ case (leftColName, rightColName) =>
        Join.ColumnPair()
          .setLeftColumn(NameSingleColumnSelection(leftColName))
          .setRightColumn(NameSingleColumnSelection(rightColName))
      })

    val paramsById = idsLeft.zip(idsRight).map({ case (leftColId, rightColId) =>
      Join.ColumnPair()
        .setLeftColumn(IndexSingleColumnSelection(leftColId))
        .setRightColumn(IndexSingleColumnSelection(rightColId))
    })

    operation.setJoinColumns(paramsByName ++ paramsById)
    leftPrefix.foreach(p => operation.setLeftPrefix(p))
    rightPrefix.foreach(p => operation.setRightPrefix(p))

    operation
  }

  private def joinWithMultipleColumnSelection(
      names: Set[String], ids: Set[Int], leftPrefix: Option[String] = leftTablePrefix,
      rightPrefix: Option[String] = rightTablePrefix): Join = {
    val namesVector = names.toVector
    val idsVector = ids.toVector
    joinWithMultipleColumnSelection(
      namesVector, idsVector, namesVector, idsVector, leftPrefix, rightPrefix)
  }

  private def appendPrefix(schema: Seq[StructField], prefix: Option[String]) = {
    schema.map(field => field.copy(name = prefix.getOrElse("") + field.name))
  }

}
