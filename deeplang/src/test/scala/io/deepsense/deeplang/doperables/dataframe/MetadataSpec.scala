/*
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.doperables.dataframe

import io.deepsense.deeplang.UnitSpec
import io.deepsense.deeplang.doperables.dataframe.types.categorical.CategoricalMapper._
import io.deepsense.deeplang.doperables.dataframe.types.categorical.{CategoriesMapping, MappingMetadataConverter}
import io.deepsense.deeplang.parameters.ColumnType
import org.apache.spark.sql.types._
import spray.json._

class MetadataSpec extends UnitSpec {

  "Metadata" should {

    val mappings: CategoricalMappingsMap = Map(
      1 -> CategoriesMapping(Seq("A", "B", "C")),
      3 -> CategoriesMapping(Seq("cat", "dog"))
    )

    val schema = StructType(Seq(
      StructField(
        "num_column",
        DoubleType),
      StructField(
        "categorical_1",
        IntegerType,
        metadata = MappingMetadataConverter.mappingToMetadata(mappings(1))),
      StructField(
        "string_column",
        StringType),
      StructField(
        "categorical_2",
        IntegerType,
        metadata = MappingMetadataConverter.mappingToMetadata(mappings(3)))
    ))

    val metadata = DataFrameMetadata(
      isExact = true,
      isColumnCountExact = true,
      columns = Seq(
        ColumnMetadata(name = Some("num_column"), columnType = Some(ColumnType.numeric)),
        ColumnMetadata(name = Some("categorical_1"), columnType = Some(ColumnType.categorical)),
        ColumnMetadata(name = Some("string_column"), columnType = Some(ColumnType.string)),
        ColumnMetadata(name = Some("categorical_2"), columnType = Some(ColumnType.categorical))
      ),
      categoricalMappings = mappings
    )

    "be extracted from schema" in {
      DataFrameMetadata.fromSchema(schema) shouldBe metadata
    }
    "produce schema" in {
      metadata.toSchema shouldBe schema
    }

  }
}
