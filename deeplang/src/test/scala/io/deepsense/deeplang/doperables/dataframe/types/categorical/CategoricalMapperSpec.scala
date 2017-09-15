/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.doperables.dataframe.types.categorical

import org.apache.spark.sql.types._

import io.deepsense.deeplang.UnitSpec
import io.deepsense.deeplang.doperables.dataframe.types.categorical.CategoricalMapper.CategoricalMappingsMap

class CategoricalMapperSpec extends UnitSpec {

  "CategoricalMapper" should {

    val mappings: CategoricalMappingsMap = Map(
      "categorical_1" -> CategoriesMapping(Seq("A", "B", "C")),
      "categorical_2" -> CategoriesMapping(Seq("cat", "dog"))
    )

    val schema = StructType(Seq(
      StructField("some_column", DoubleType),
      StructField("categorical_1", IntegerType),
      StructField("categorical_2", IntegerType)
    ))

    val categorizedSchema = StructType(Seq(
      StructField("some_column", DoubleType),
      StructField(
        "categorical_1",
        IntegerType,
        metadata = MappingMetadataConverter.mappingToMetadata(mappings("categorical_1"))),
      StructField(
        "categorical_2",
        IntegerType,
        metadata = MappingMetadataConverter.mappingToMetadata(mappings("categorical_2")))
    ))

    "update schema based on mappings" in {
      CategoricalMapper.categorizedSchema(schema, mappings) shouldBe categorizedSchema
    }
    "create mappings based on schema" in {
      CategoricalMapper.mappingsMapFromSchema(categorizedSchema) shouldBe mappings
    }
  }
}
