/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.entitystorage.json

import org.scalatest.{FlatSpec, Matchers}
import spray.json._

import io.deepsense.commons.datetime.DateTimeConverter
import io.deepsense.entitystorage.factories.EntityTestFactory
import io.deepsense.models.entities._

class EntityJsonProtocolSpec
  extends FlatSpec
  with Matchers
  with EntityJsonProtocol
  with EntityTestFactory {

  "EntityWithReport" should "be correctly serialized to Json" in {
    val entity = testEntityWithReport()
    entity.toJson shouldBe jsonEntity(entity)
  }

  it should "be correctly deserialized from Json" in {
    val entity = testEntityWithReport()
    val json = jsonEntity(entity)
    json.convertTo[EntityWithReport] shouldBe entity
  }

  "EntityInfo" should "be correctly serialized to Json" in {
    val entity = testEntityWithReport()
    entity.info.toJson shouldBe jsonEntityInfo(entity.info)
  }

  it should "be correctly deserialized from Json" in {
    val entity = testEntityWithReport()
    val json = jsonEntityInfo(entity.info)
    json.convertTo[EntityInfo] shouldBe entity.info
  }

  "EntityUpdate" should "be correctly serialized to Json" in {
    val entityUpdate = testEntityUpdate()
    entityUpdate.toJson shouldBe jsonEntityUpdate(entityUpdate)
  }

  it should "be correctly deserialized from Json" in {
    val entityUpdate = testEntityUpdate()
    val json = jsonEntityUpdate(entityUpdate)
    json.convertTo[EntityUpdate] shouldBe entityUpdate
  }

  private def jsonEntity(entity: EntityWithReport): JsObject = JsObject(entityMap(entity))

  private def jsonEntityInfo(entity: EntityInfo): JsObject = JsObject(entityInfoMap(entity))

  private def jsonEntityUpdate(entity: EntityUpdate): JsObject =
    JsObject(entityUpdateMap(entity))

  private def entityMap(entity: EntityWithReport): Map[String, JsValue] =
    Map("info" -> jsonEntityInfo(entity.info), "report" -> jsonReport(entity.report))

  private def entityInfoMap(entityInfo: EntityInfo): Map[String, JsValue] = Map(
    "id" -> JsString(entityInfo.id.value.toString),
    "name" -> JsString(entityInfo.name),
    "description" -> JsString(entityInfo.description),
    "saved" -> JsBoolean(entityInfo.saved),
    "tenantId" -> JsString(entityInfo.tenantId),
    "dClass" -> JsString(entityInfo.dClass),
    "created" -> JsString(DateTimeConverter.toString(entityInfo.created)),
    "updated" -> JsString(DateTimeConverter.toString(entityInfo.updated)))

  private def entityUpdateMap(entityUpdate: EntityUpdate): Map[String, JsValue] = Map(
    "name" -> JsString(entityUpdate.name),
    "description" -> JsString(entityUpdate.description),
    "saved" -> JsBoolean(entityUpdate.saved))

  private def jsonReport(report: DataObjectReport) = JsString(report.jsonReport)
}
