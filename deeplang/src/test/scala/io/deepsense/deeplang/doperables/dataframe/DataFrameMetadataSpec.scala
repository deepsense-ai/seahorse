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

package io.deepsense.deeplang.doperables.dataframe

import org.apache.spark.sql.types._

import io.deepsense.commons.types.ColumnType
import io.deepsense.deeplang.UnitSpec
import io.deepsense.deeplang.doperables.dataframe.types.categorical.{CategoriesMapping, MappingMetadataConverter}
import io.deepsense.deeplang.doperations.exceptions.{ColumnDoesNotExistException, ColumnsDoNotExistException}
import io.deepsense.deeplang.inference.exceptions.NameNotUniqueException
import io.deepsense.deeplang.inference.{MultipleColumnsMayNotExistWarning, SingleColumnMayNotExistWarning}
import io.deepsense.deeplang.parameters._

class DataFrameMetadataSpec extends UnitSpec {

  "DataFrameMetadata" should {

    val mappings = List(
      CategoriesMapping(Seq("A", "B", "C")),
      CategoriesMapping(Seq("cat", "dog"))
    )

    val schema = StructType(Seq(
      StructField(
        "num_column",
        DoubleType),
      StructField(
        "categorical_1",
        IntegerType,
        metadata = MappingMetadataConverter.mappingToMetadata(mappings(0))),
      StructField(
        "string_column",
        StringType),
      StructField(
        "categorical_2",
        IntegerType,
        metadata = MappingMetadataConverter.mappingToMetadata(mappings(1)))
    ))

    val metadata = DataFrameMetadata(
      isExact = true,
      isColumnCountExact = true,
      columns = Map(
        "num_column" -> CommonColumnMetadata(
          name = "num_column", index = Some(0), columnType = Some(ColumnType.numeric)),
        "categorical_1" -> CategoricalColumnMetadata(
          name = "categorical_1", index = Some(1), categories = Some(mappings(0))),
        "string_column" -> CommonColumnMetadata(
          name = "string_column", index = Some(2), columnType = Some(ColumnType.string)),
        "categorical_2" -> CategoricalColumnMetadata(
          name = "categorical_2", index = Some(3), categories = Some(mappings(1)))
      )
    )

    "be extracted from schema" in {
      DataFrameMetadata.fromSchema(schema) shouldBe metadata
    }
    "produce schema" in {
      metadata.toSchema shouldBe schema
    }

    "select method applied on fuzzyMetadata" should {

      "return proper columns and no warnings when selector is correct" in {
        val nameSelection = NameColumnSelection(Set(categoricalColumnWithUnknownIndex.name))
        val selection = MultipleColumnSelection(Vector(nameSelection))
        val (columns, warnings) = fuzzyMetadata.select(selection)
        columns should have size 1
        columns(0) shouldBe categoricalColumnWithUnknownIndex
        warnings.warnings shouldBe empty
      }

      "return empty selection and warnings when selector may be incorrect" in {
        val nameSelection = NameColumnSelection(Set(numericColumn.name))
        val selection = MultipleColumnSelection(Vector(nameSelection))
        val (columns, warnings) = fuzzyMetadata.select(selection)
        columns shouldBe empty
        warnings.warnings should have size 1
        warnings.warnings.head shouldBe MultipleColumnsMayNotExistWarning(
          nameSelection,
          fuzzyMetadata)
      }

      "return some columns and warnings when selector is partially correct" in {
        // index 4 points to categoricalColumnWithUnknownCategories
        // index 5 points to columnWithUnknownType
        // index 6 does not exists
        // 2 columns and 1 warning should be produced
        val rangeSelection = IndexRangeColumnSelection(Some(4), Some(6))

        val selection = MultipleColumnSelection(Vector(rangeSelection))
        val (columns, warnings) = fuzzyMetadata.select(selection)
        columns should have size 2
        columns shouldBe Seq(categoricalColumnWithUnknownCategories, columnWithUnknownType)
        warnings.warnings should have size 1
        warnings.warnings.head shouldBe MultipleColumnsMayNotExistWarning(
          rangeSelection,
          fuzzyMetadata)
      }

      "return selection and warnings when some selectors may be incorrect" in {
        // numericColumn is not in fuzzyMetadata, stringColumn is
        // (1 column, 1 warning from this selection)
        val nameSelection = NameColumnSelection(Set(numericColumn.name, stringColumn.name))

        // both indexes are in the fuzzy metadata
        // (2 columns, 0 warnings from this selection)
        val indexSelection1 = IndexColumnSelection(
          Set(categoricalColumn1.index.get, columnWithUnknownType.index.get))

        // categoricalColumn2 is not in fuzzy metadata
        // (0 columns, 1 warning from this selection)
        val indexSelection2 = IndexColumnSelection(Set(categoricalColumn2.index.get))

        val selection = MultipleColumnSelection(
          Vector(nameSelection, indexSelection1, indexSelection2))

        // select should produce 3 columns and 2 warnings
        val (columns, warnings) = fuzzyMetadata.select(selection)

        columns should have size 3
        columns shouldBe Seq(categoricalColumn1, stringColumn, columnWithUnknownType)
        warnings.warnings should have size 2
        warnings.warnings.toSet shouldBe Set(
          MultipleColumnsMayNotExistWarning(nameSelection, fuzzyMetadata),
          MultipleColumnsMayNotExistWarning(indexSelection2, fuzzyMetadata))
      }

      "return columns without duplicates" in {
        // numericColumn is not in fuzzyMetadata, stringColumn is
        // (1 column, 1 warning from this selection)
        val nameSelection = NameColumnSelection(Set(numericColumn.name, stringColumn.name))

        // string column is in fuzzy metadata (but already selected by nameSelection)
        // (0 columns, 0 warning from this selection)
        val indexSelection = IndexColumnSelection(Set(stringColumn.index.get))

        val selection = MultipleColumnSelection(Vector(nameSelection, indexSelection))

        // select should produce 1 columns and 1 warnings
        val (columns, warnings) = fuzzyMetadata.select(selection)

        columns should have size 1
        columns.head shouldBe stringColumn
        warnings.warnings should have size 1
        warnings.warnings.head shouldBe MultipleColumnsMayNotExistWarning(
          nameSelection,
          fuzzyMetadata)
      }

      "return column without warnings when correct single name selector used" in {
        val nameSelector = NameSingleColumnSelection(columnWithUnknownType.name)
        val (selection, warnings) = fuzzyMetadata.select(nameSelector)
        selection shouldBe defined
        selection.get shouldBe columnWithUnknownType
        warnings.warnings shouldBe empty
      }

      "return column without warnings when correct single index selector used" in {
        val indexSelector = IndexSingleColumnSelection(columnWithUnknownType.index.get)
        val (selection, warnings) = fuzzyMetadata.select(indexSelector)
        selection shouldBe defined
        selection.get shouldBe columnWithUnknownType
        warnings.warnings shouldBe empty
      }

      "return no columns and warning when possibly incorrect single selector used" in {
        val indexSelector = IndexSingleColumnSelection(numericColumn.index.get)
        val (selection, warnings) = fuzzyMetadata.select(indexSelector)
        selection shouldBe None
        warnings.warnings should have size 1
        warnings.warnings.head shouldBe SingleColumnMayNotExistWarning(
          indexSelector,
          fuzzyMetadata)
      }
    }

    "select method applied on exact metadata" should {

      "return proper columns and no warnings when selector is correct" in {
        val typeSelection = TypeColumnSelection(Set(numericColumn.columnType.get))
        val selection = MultipleColumnSelection(Vector(typeSelection))
        val (columns, warnings) = metadata.select(selection)
        columns should have size 1
        columns.head shouldBe numericColumn
        warnings.warnings shouldBe empty
      }

      "return proper columns and no warnings for excluding selector" in {
        val typeSelection = TypeColumnSelection(Set(numericColumn.columnType.get))
        val selection = MultipleColumnSelection(Vector(typeSelection), true)
        val (columns, warnings) = metadata.select(selection)
        columns should have size 3
        columns shouldNot contain(numericColumn)
        warnings.warnings shouldBe empty
      }

      "throw exception when incorrect selection applied" in {
        val nameSelection = NameColumnSelection(Set(columnWithUnknownType.name))
        val selection = MultipleColumnSelection(Vector(nameSelection))
        val exception = the [ColumnsDoNotExistException] thrownBy {
          metadata.select(selection)
        }
        exception.invalidSelections should have size 1
        exception.invalidSelections.head shouldBe nameSelection
        exception.schema shouldBe schema
      }

      "return proper columns and no warnings when multiple selectors are correct" in {
        // 1 column selected
        val typeSelection = TypeColumnSelection(Set(numericColumn.columnType.get))
        // 3 columns selected but numericColumn duplicated with typeSelection
        val nameSelection = NameColumnSelection(
          Set(categoricalColumn1.name, categoricalColumn2.name, numericColumn.name))

        val selection = MultipleColumnSelection(Vector(typeSelection, nameSelection))
        val (columns, warnings) = metadata.select(selection)
        columns should have size 3
        columns shouldBe Seq(numericColumn, categoricalColumn1, categoricalColumn2)
        warnings.warnings shouldBe empty
      }

      "return proper columns and no warnings when multiple excluding selectors are correct" in {
        // 1 column selected
        val typeSelection = TypeColumnSelection(Set(numericColumn.columnType.get))
        // 3 columns selected but numericColumn duplicated with typeSelection
        val nameSelection = NameColumnSelection(
          Set(categoricalColumn1.name, categoricalColumn2.name, numericColumn.name))

        val selection = MultipleColumnSelection(Vector(typeSelection, nameSelection), true)
        val (columns, warnings) = metadata.select(selection)
        columns should contain noneOf (numericColumn, categoricalColumn1, categoricalColumn2)
        warnings.warnings shouldBe empty
      }

      "return column without warnings when correct name selector used" in {
        val nameSelector = NameSingleColumnSelection(numericColumn.name)
        val (selection, warnings) = metadata.select(nameSelector)
        selection shouldBe defined
        selection.get shouldBe numericColumn
        warnings.warnings shouldBe empty
      }

      "return column without warnings when correct index selector used" in {
        val indexSelector = IndexSingleColumnSelection(categoricalColumn2.index.get)
        val (selection, warnings) = metadata.select(indexSelector)
        selection shouldBe defined
        selection.get shouldBe categoricalColumn2
        warnings.warnings shouldBe empty
      }

      "throw exception when incorrect selector used" in {
        val indexSelector = IndexSingleColumnSelection(columnWithUnknownType.index.get)
        val exception = the [ColumnDoesNotExistException] thrownBy {
          metadata.select(indexSelector)
        }
        exception.dataFrameMetadata shouldBe metadata
        exception.selection shouldBe indexSelector
      }
    }
  }

  val mappings = List(
    CategoriesMapping(Seq("A", "B", "C")),
    CategoriesMapping(Seq("cat", "dog"))
  )

  val numericColumn = CommonColumnMetadata(
    name = "num_column", index = Some(0), columnType = Some(ColumnType.numeric))

  val categoricalColumn1 = CategoricalColumnMetadata(
    name = "categorical_1", index = Some(1), categories = Some(mappings(0)))

  val stringColumn = CommonColumnMetadata(
    name = "string_column", index = Some(2), columnType = Some(ColumnType.string))

  val categoricalColumn2 = CategoricalColumnMetadata(
    name = "categorical_2", index = Some(3), categories = Some(mappings(1)))

  val numericColumnWithUnknownIndex = CommonColumnMetadata(
    name = "num_unknown_index", index = None, columnType = Some(ColumnType.numeric))

  val categoricalColumnWithUnknownIndex = CategoricalColumnMetadata(
    name = "categorical_unknown_index", index = None, categories = Some(mappings(1)))

  val categoricalColumnWithUnknownCategories = CategoricalColumnMetadata(
    name = "categorical_unknown_categories", index = Some(4), categories = None)

  val columnWithUnknownType = CommonColumnMetadata(
    name = "unknown_type", index = Some(5), columnType = None)


  val schema = StructType(Seq(
    StructField(
      numericColumn.name,
      DoubleType),
    StructField(
      categoricalColumn1.name,
      IntegerType,
      metadata = MappingMetadataConverter.mappingToMetadata(mappings(0))),
    StructField(
      stringColumn.name,
      StringType),
    StructField(
      categoricalColumn2.name,
      IntegerType,
      metadata = MappingMetadataConverter.mappingToMetadata(mappings(1)))
  ))

  val metadata = DataFrameMetadata(
    isExact = true,
    isColumnCountExact = true,
    columns = DataFrameMetadata.buildColumnsMap(Seq(
      numericColumn,
      categoricalColumn1,
      stringColumn,
      categoricalColumn2)
    )
  )

  val fuzzyMetadata = DataFrameMetadata(
    isExact = false,
    isColumnCountExact = false,
    columns = DataFrameMetadata.buildColumnsMap(Seq(
      categoricalColumn1,
      stringColumn,
      numericColumnWithUnknownIndex,
      categoricalColumnWithUnknownIndex,
      categoricalColumnWithUnknownCategories,
      columnWithUnknownType
    ))
  )

  "DataFrameMetadata.appendColumn" should {
    "add column with correct index" when {
      "columnsCount is not exact" in {

        val metadata = DataFrameMetadata(
          isExact = false,
          isColumnCountExact = false,
          columns = Map(
            "num_col" -> CommonColumnMetadata("num_col", Some(0), Some(ColumnType.numeric))))

        val columnToAdd = CommonColumnMetadata("some_col", Some(100), Some(ColumnType.string))

        val expectedMetadata = DataFrameMetadata(
          isExact = false,
          isColumnCountExact = false,
          columns = Map(
            "num_col" -> CommonColumnMetadata("num_col", Some(0), Some(ColumnType.numeric)),
            "some_col" -> CommonColumnMetadata("some_col", None, Some(ColumnType.string))))

        metadata.appendColumn(columnToAdd) shouldBe expectedMetadata
      }
      "columnsCount is exact" in {

        val metadata = DataFrameMetadata(
          isExact = false,
          isColumnCountExact = true,
          columns = Map(
            "num_col" -> CommonColumnMetadata("num_col", Some(0), Some(ColumnType.numeric))))

        val columnToAdd = CommonColumnMetadata("some_col", Some(100), Some(ColumnType.string))

        val expectedMetadata = DataFrameMetadata(
          isExact = false,
          isColumnCountExact = true,
          columns = Map(
            "num_col" -> CommonColumnMetadata("num_col", Some(0), Some(ColumnType.numeric)),
            "some_col" -> CommonColumnMetadata("some_col", Some(1), Some(ColumnType.string))))

        metadata.appendColumn(columnToAdd) shouldBe expectedMetadata
      }
    }
    "throw an exception" when {
      "added column name is not unique" in {
        val name = "num_col"
        val metadata = DataFrameMetadata(
          isExact = false,
          isColumnCountExact = false,
          columns = Map(
            name -> CommonColumnMetadata(name, Some(0), Some(ColumnType.numeric))))

        val columnToAdd = CommonColumnMetadata(name, None, Some(ColumnType.string))

        val exception = the [NameNotUniqueException] thrownBy {
          metadata.appendColumn(columnToAdd)
        }
        exception.name shouldBe name
      }
    }
  }
}
