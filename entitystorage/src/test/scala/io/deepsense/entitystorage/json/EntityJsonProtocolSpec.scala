/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 *  Owner: Rafal Hryciuk
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

  "Entity" should "be correctly serialized to Json when there is data and report" in {
    val entity = testEntity

    entity.toJson should be (jsonEntity(entity))
  }

  it should "be correctly serialized to Json when there is only Report" in {
    val entity = testEntity(None, Some(testDataObjectReport))

    entity.toJson should be (jsonEntity(entity))
  }

  it should "be correctly deserialized from Json when there is data and reference" in {
    val entity = testEntity
    val json = jsonEntity(entity)

    json.convertTo[Entity] should be (entity)
  }

  it should "be correctly deserialized from Json when there is only report" in {
    val entity = testEntity(None, Some(testDataObjectReport))
    val json = jsonEntity(entity)

    json.convertTo[Entity] should be (entity)
  }

  "EntityDescriptor" should "be correctly serialized to Json" in {
    val entity = testEntity
    val entityDescriptor = CompactEntityDescriptor(entity)

    entityDescriptor.toJson should be (jsonEntityDescriptor(entity))
  }

  it should "be correctly deserialized from Json" in {
    val entity = testEntity
    val entityDescriptor = CompactEntityDescriptor(entity)
    val json = jsonEntityDescriptor(entity)

    json.convertTo[CompactEntityDescriptor] should be (entityDescriptor)
  }

  "UserEntityDescription" should "be correctly serialized to Json" in {
    val entity = testEntity
    val userEntityDescription = UserEntityDescriptor(
      entity.id,
      entity.name,
      entity.description,
      entity.saved)

    userEntityDescription.toJson should be (jsonUserEntityDescription(entity))
  }

  it should "be correctly deserialized from Json" in {
    val entity = testEntity
    val userEntityDescription = UserEntityDescriptor(
      entity.id,
      entity.name,
      entity.description,
      entity.saved)
    val json = jsonUserEntityDescription(entity)

    json.convertTo[UserEntityDescriptor] should be (userEntityDescription)
  }

  private def jsonEntity(entity: Entity): JsObject = JsObject(entityMap(entity))

  private def jsonEntityDescriptor(entity: Entity): JsObject = JsObject(entityDescriptorMap(entity))

  private def jsonUserEntityDescription(entity: Entity): JsObject =
    JsObject(userEntityDescriptionMap(entity))

  private def entityMap(entity: Entity): Map[String, JsValue] =
    entityDescriptorMap(entity) ++ Map("report" -> jsonReport(entity.report.get)) ++
      (if (entity.data.isDefined) Map("data" -> jsonReference(entity.data.get)) else Map())

  private def entityDescriptorMap(entity: Entity): Map[String, JsValue] =
    userEntityDescriptionMap(entity) ++ Map(
      "tenantId" -> JsString(entity.tenantId),
      "dClass" -> JsString(entity.dClass),
      "created" -> JsString(DateTimeConverter.toString(entity.created)),
      "updated" -> JsString(DateTimeConverter.toString(entity.updated)))

  private def userEntityDescriptionMap(entity: Entity): Map[String, JsValue] = Map(
    "id" -> JsString(entity.id.value.toString),
    "name" -> JsString(entity.name),
    "description" -> JsString(entity.description),
    "saved" -> JsBoolean(entity.saved))

  private def jsonReport(report: DataObjectReport) = JsString(report.message)

  private def jsonReference(reference: DataObjectReference) =
    JsObject(Map("url" -> JsString(reference.url)))
}
