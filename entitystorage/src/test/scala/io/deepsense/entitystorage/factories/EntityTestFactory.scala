/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 *  Owner: Rafal Hryciuk
 */

package io.deepsense.entitystorage.factories

import io.deepsense.commons.datetime.DateTimeConverter
import io.deepsense.entitystorage.models.{DataObject, Entity}

trait EntityTestFactory {

  def testEntity(dClass: String, data: DataObject) = {
    val now = DateTimeConverter.now
    Entity(
      Entity.Id.randomId,
      "testEntity",
      "entity Description",
      dClass,
      now,
      now.plusHours(1),
      data)
  }
}
