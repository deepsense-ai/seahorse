/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.entitystorage.models

import org.scalatest.{FlatSpec, Matchers}

import io.deepsense.commons.serialization.Serialization
import io.deepsense.entitystorage.factories.EntityTestFactory
import io.deepsense.models.entities.{EntityWithData, EntityWithReport, Entity}

class EntitySerializationSpec
  extends FlatSpec
  with Matchers
  with Serialization
  with EntityTestFactory {

  "EntityWithReport" should "serialize and deserialize correctly" in  {
    val entity = testEntityWithReport()

    val serialized = serialize(entity)
    val deserialized = deserialize[EntityWithReport](serialized)

    deserialized shouldBe entity
  }

  "EntityWithData" should "serialize and deserialize correctly" in  {
    val entity = testEntityWithData()

    val serialized = serialize(entity)
    val deserialized = deserialize[EntityWithData](serialized)

    deserialized shouldBe entity
  }
}
