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
