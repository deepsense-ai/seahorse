/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.doperables.dataframe

import io.deepsense.deeplang.UnitSpec
import io.deepsense.deeplang.doperables.dataframe.types.categorical.CategoricalMapper._
import io.deepsense.deeplang.doperables.dataframe.types.categorical.{CategoriesMapping, MappingMetadataConverter}
import io.deepsense.deeplang.parameters.ColumnType
import org.apache.spark.sql.types._
import spray.json._

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

  }
}
