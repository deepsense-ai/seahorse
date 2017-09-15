/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.doperables.dataframe.types.categorical

import org.apache.spark.sql.types.MetadataBuilder

import io.deepsense.deeplang.UnitSpec

class MappingMetadataConverterSpec extends UnitSpec {

  val idToValue = Map(2 -> "two", 20 -> "twenty", 1337 -> "a lot")
  val valueToId = idToValue.map(_.swap)
  val mapping = CategoriesMapping(valueToId, idToValue)

  "MappingMetadataConverter" should {
    "convert metadata to mapping and the other way around" in {
      val oldMetadata = new MetadataBuilder().build()
      val metadata = MappingMetadataConverter.mappingToMetadata(mapping, oldMetadata)
      val readMapping = MappingMetadataConverter.mappingFromMetadata(metadata)
      readMapping.get shouldBe mapping
    }
  }
}
