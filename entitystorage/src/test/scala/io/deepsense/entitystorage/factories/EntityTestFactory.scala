/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 *  Owner: Rafal Hryciuk
 */

package io.deepsense.entitystorage.factories

import io.deepsense.commons.datetime.DateTimeConverter
import io.deepsense.entitystorage.models.{InputEntity, DataObjectReference, DataObjectReport, Entity}

trait EntityTestFactory extends DataObjectFactory {

  def testEntity: Entity = testEntity(Some(testDataObjectReference), Some(testDataObjectReport))

  def testEntity(data: Option[DataObjectReference], report: Option[DataObjectReport]): Entity =
    testEntity("testTenantId", 0, data, report)

  def testEntity(
      tenantId: String,
      index: Int,
      data: Option[DataObjectReference],
      report: Option[DataObjectReport]): Entity = {
    val now = DateTimeConverter.now
    Entity(
      tenantId,
      Entity.Id.randomId,
      s"testEntity_$index",
      s"entity Description_$index",
      "DataFrame",
      data,
      report,
      now,
      now)
  }

  def testInputEntity: InputEntity = new InputEntity(
    "Mr Crowley",
    "Operation",
    "super operation",
    "DataFrame",
    Some(testDataObjectReference),
    Some(testDataObjectReport),
    false)
}
