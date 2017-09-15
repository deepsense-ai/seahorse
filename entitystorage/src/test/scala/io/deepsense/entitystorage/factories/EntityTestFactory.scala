/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 */

package io.deepsense.entitystorage.factories

import io.deepsense.commons.datetime.DateTimeConverter
import io.deepsense.models.entities._

trait EntityTestFactory extends DataObjectTestFactory {

  val testTenantId = "Mr Crowley"
  val testName = "Operation"
  val testDescription = "description"
  val testDClass = "DataFrame"

  def testEntity: Entity = testEntity(Some(testDataObjectReference), Some(testDataObjectReport))

  def testEntity(data: Option[DataObjectReference], report: Option[DataObjectReport]): Entity =
    testEntity(testTenantId, 0, data, report)

  def testEntity(
      tenantId: String,
      index: Int,
      data: Option[DataObjectReference],
      report: Option[DataObjectReport]): Entity = {
    val now = DateTimeConverter.now
    Entity(
      tenantId,
      Entity.Id.randomId,
      indexedValue(testName, index),
      indexedValue(testDescription, index),
      testDClass,
      data,
      report,
      now,
      now)
  }

  def testInputEntity: InputEntity = new InputEntity(
    testTenantId,
    testName,
    testDescription,
    testDClass,
    Some(testDataObjectReference),
    Some(testDataObjectReport),
    false)

  private def indexedValue(value: String, index: Int): String =
    if (index == 0) value else s"${value}_$index"
}
