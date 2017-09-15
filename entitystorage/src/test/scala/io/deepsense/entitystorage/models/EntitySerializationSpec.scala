/**
 * Copyright (c) 2015, CodiLime, Inc.
 */

package io.deepsense.entitystorage.models

import org.scalatest.{FlatSpec, Matchers}

import io.deepsense.commons.serialization.Serialization
import io.deepsense.entitystorage.factories.EntityTestFactory
import io.deepsense.models.entities.Entity

class EntitySerializationSpec
  extends FlatSpec
  with Matchers
  with Serialization
  with EntityTestFactory {

  "Entity" should "serialize and deserialize correctly when has reference and report" in  {
    val entity = testEntity

    val serialized = serialize(entity)
    val deserialized = deserialize[Entity](serialized)

    deserialized shouldBe entity
  }
}
