/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.doperables.dataframe.types.categorical

import io.deepsense.deeplang.UnitSpec

class MappingSpec extends UnitSpec {

  val values = Seq("Value1", "Value2", "Value3", "Value4")
  val mapping = CategoriesMapping(values)

  "Mapping" should {
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
  }
}
