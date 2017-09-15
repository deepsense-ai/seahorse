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

import io.deepsense.deeplang.UnitSpec

class CategoriesMappingSpec extends UnitSpec {

  val values = Seq("Value1", "Value2", "Value3", "Value4")
  val mapping = CategoriesMapping(values)

  "CategoriesMapping" should {
    "create a proper mapping" in {
      values.foreach(v => mapping.idToValue(mapping.valueToId(v)) shouldBe v)
      mapping.idToValue should have size values.size
      mapping.valueToId should have size values.size
      mapping.ids should contain theSameElementsAs (0 to 3).toSet
      mapping.values should contain theSameElementsAs values
    }
    "create the same mapping when merged with empty mapping" in {
      val mergedMapping = mapping.mergeWith(CategoriesMapping.empty)
      mergedMapping.finalMapping shouldBe mapping
      mergedMapping.otherToFinal shouldBe empty
    }
    "create the 'other' mapping when empty merged with 'other'" in {
      val mergedMapping = CategoriesMapping.empty.mergeWith(mapping)
      mergedMapping.finalMapping shouldBe mapping
      forAll(mapping.ids){ v => mergedMapping.otherToFinal.mapId(v) shouldBe v }
    }
    "properly merge with non-empty mapping" in {
      val otherValues = Seq("OtherValue0", "Value2", "Value1", "OtherValue1", "OtherValue2")
      val otherMapping = CategoriesMapping(otherValues)
      val mergedMapping = mapping.mergeWith(otherMapping)
      val finalMapping = mergedMapping.finalMapping
      val otherToMerged = mergedMapping.otherToFinal
      finalMapping.values should contain theSameElementsAs (values ++ otherValues).toSet
      finalMapping.values.foreach(v =>
        finalMapping.idToValue(finalMapping.valueToId(v)) shouldBe v)
      forAll(values){ v => finalMapping.valueToId(v) shouldBe mapping.valueToId(v) }
      forAll(otherValues){ value =>
        val idInOtherMapping = otherMapping.valueToId(value)
        val idInFinalMapping = otherToMerged.mapId(idInOtherMapping)
        val valueInFinalMapping = finalMapping.idToValue(idInFinalMapping)
        valueInFinalMapping shouldBe value
      }
    }
    "disallow to create a mapping with a null" in {
      an[IllegalArgumentException] should be thrownBy CategoriesMapping(Seq("a", null, "b"))
    }
    "sort values alphabetically" in {
      val values = Seq("a", "Z", "t", "w", "A")
      val orderedValues = Seq( "A", "Z", "a", "t", "w")
      val m = CategoriesMapping(values)
      m.values shouldBe orderedValues
    }
  }
}
