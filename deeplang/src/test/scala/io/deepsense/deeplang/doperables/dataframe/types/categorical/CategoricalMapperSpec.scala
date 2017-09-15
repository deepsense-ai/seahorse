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
