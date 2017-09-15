/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 *  Owner: Rafal Hryciuk
 */

package io.deepsense.entitystorage.models

import org.scalatest.{FlatSpec, Matchers}

import io.deepsense.commons.serialization.Serialization
import io.deepsense.entitystorage.factories.EntityTestFactory

class EntitySerializationSpec
  extends FlatSpec
  with Matchers
  with Serialization
  with EntityTestFactory {

  "Entity" should "serialize and deserialize correctly when has reference" in  {
    val entity = testEntity("DataFrame", DataObjectReference("some sort of url"))

    val serialized = serialize(entity)
    val deserialized = deserialize[Entity](serialized)

    deserialized shouldBe entity
  }
}
