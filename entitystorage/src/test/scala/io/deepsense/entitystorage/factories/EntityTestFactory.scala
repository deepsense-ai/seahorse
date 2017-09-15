/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.entitystorage.factories

import io.deepsense.commons.datetime.DateTimeConverter
import io.deepsense.models.entities._

trait EntityTestFactory extends DataObjectTestFactory {

  val testTenantId = "Mr Crowley"
  val testName = "test name of entity"
  val testDescription = "test description of entity"
  val testDClass = "test dClass for entity"

  def testEntityWithData(
      tenantId: String = testTenantId,
      index: Int = 0,
      data: DataObjectReference = testDataObjectReference): EntityWithData =
    EntityWithData(testEntityInfo(tenantId, index), data)

  def testEntityWithReport(
      tenantId: String = testTenantId,
      index: Int = 0,
      report: DataObjectReport = testDataObjectReport): EntityWithReport =
    EntityWithReport(testEntityInfo(tenantId, index), report)

  def testEntityInfo(tenantId: String = testTenantId, index: Int = 0): EntityInfo = {
    val now = DateTimeConverter.now
    EntityInfo(
      Entity.Id.randomId, tenantId, indexedValue(testName, index),
      indexedValue(testDescription, index), testDClass, now, now)
  }

  def testEntityCreate(
      tenantId: String = testTenantId,
      index: Int = 0,
      data: Option[DataObjectReference] = Some(testDataObjectReference),
      report: DataObjectReport = testDataObjectReport): EntityCreate =
    EntityCreate(info = testEntityInfo(tenantId, index), data, report)

  def testEntityUpdate(): EntityUpdate = EntityUpdate(testEntityCreate())

  private def indexedValue(value: String, index: Int): String =
    if (index == 0) value else s"${value}_$index"

  def modifyEntity(entity: EntityUpdate): EntityUpdate = {
    val s = "_modified"
    entity.copy(
      name = entity.name + s,
      description = entity.description + s,
      saved = !entity.saved)
  }
}
