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
      "categorical_1" -> CategoriesMapping(Seq("A", "B", "C")),
      "categorical_2" -> CategoriesMapping(Seq("cat", "dog"))
    )

    val schema = StructType(Seq(
      StructField(
        "num_column",
        DoubleType),
      StructField(
        "categorical_1",
        IntegerType,
        metadata = MappingMetadataConverter.mappingToMetadata(mappings("categorical_1"))),
      StructField(
        "string_column",
        StringType),
      StructField(
        "categorical_2",
        IntegerType,
        metadata = MappingMetadataConverter.mappingToMetadata(mappings("categorical_2")))
    ))

    val metadata = DataFrameMetadata(
      isExact = true,
      isColumnCountExact = true,
      columns = Map(
        "num_column" -> ColumnMetadata(
          name = "num_column", index = Some(0), columnType = Some(ColumnType.numeric)),
        "categorical_1" -> ColumnMetadata(
          name = "categorical_1", index = Some(1), columnType = Some(ColumnType.categorical)),
        "string_column" -> ColumnMetadata(
          name = "string_column", index = Some(2), columnType = Some(ColumnType.string)),
        "categorical_2" -> ColumnMetadata(
          name = "categorical_2", index = Some(3), columnType = Some(ColumnType.categorical))
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
