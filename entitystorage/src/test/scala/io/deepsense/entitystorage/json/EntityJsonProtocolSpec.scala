/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 *  Owner: Rafal Hryciuk
 */

package io.deepsense.entitystorage.json

import java.util.UUID

import org.scalatest.{FlatSpec, Matchers}
import spray.json._

import io.deepsense.commons.datetime.DateTimeConverter
import io.deepsense.commons.models.Id
import io.deepsense.deeplang.doperables.Report
import io.deepsense.entitystorage.models._

class EntityJsonProtocolSpec extends FlatSpec with Matchers with EntityJsonProtocol {

  "Entity" should "be correctly serialized to Json when data is Report" in {
    val entity = testEntity("Report", DataObjectReport(Report()))

    entity.toJson should be (jsonEntity(entity))
  }

  it should "be correctly serialized to Json when data is Reference" in {
    val entity = testEntity("DataFrame", DataObjectReference("hdfs://some.host.com:8080/home"))

    entity.toJson should be (jsonEntity(entity))
  }

  it should "be correctly deserialized from Json when data is Reference" in {
    val entity = testEntity("DataFrame", DataObjectReference("hdfs://some.host.com:8080/home"))
    val json = jsonEntity(entity)

    json.convertTo[Entity] should be (entity)
  }

  it should "be correctly deserialized from Json when data is Report" in {
    val entity = testEntity("Report", DataObjectReport(Report()))
    val json = jsonEntity(entity)

    json.convertTo[Entity] should be (entity)
  }

  "EntityDescriptor" should "be correctly serialized to Json" in {
    val entity = testEntity("DataFrame", DataObjectReference("hdfs://some.host.com:8080/home"))
    val entityDescriptor = EntityDescriptor(entity)

    entityDescriptor.toJson should be (jsonEntityDescriptor(entity))
  }

  it should "be correctly deserialized from Json" in {
    val entity = testEntity("DataFrame", DataObjectReference("hdfs://some.host.com:8080/home"))
    val entityDescriptor = EntityDescriptor(entity)
    val json = jsonEntityDescriptor(entity)

    json.convertTo[EntityDescriptor] should be (entityDescriptor)
  }

  "UserEntityDescription" should "be correctly serialized to Json" in {
    val entity = testEntity("DataFrame", DataObjectReference("hdfs://some.host.com:8080/home"))
    val userEntityDescription = UserEntityDescription(
      entity.id,
      entity.name,
      entity.description,
      entity.saved)

    userEntityDescription.toJson should be (jsonUserEntityDescription(entity))
  }

  it should "be correctly deserialized from Json" in {
    val entity = testEntity("DataFrame", DataObjectReference("hdfs://some.host.com:8080/home"))
    val userEntityDescription = UserEntityDescription(
      entity.id,
      entity.name,
      entity.description,
      entity.saved)
    val json = jsonUserEntityDescription(entity)

    json.convertTo[UserEntityDescription] should be (userEntityDescription)
  }

  private def testEntity(dClass: String, data: DataObject) = {
    val now = DateTimeConverter.now
    Entity(
      Id(UUID.randomUUID()),
      "testEntity",
      "entity Description",
      dClass,
      now,
      now.plusHours(1),
      data)
  }

  private def jsonEntity(entity: Entity): JsObject = JsObject(entityMap(entity))

  private def jsonEntityDescriptor(entity: Entity): JsObject = JsObject(entityDescriptorMap(entity))

  private def jsonUserEntityDescription(entity: Entity): JsObject =
    JsObject(userEntityDescriptionMap(entity))

  private def entityMap(entity: Entity): Map[String, JsValue] = entityDescriptorMap(entity) ++ Map(
    "data" -> jsonDataObject(entity.data))

  private def entityDescriptorMap(entity: Entity): Map[String, JsValue] =
    userEntityDescriptionMap(entity) ++ Map(
      "dClass" -> JsString(entity.dClass),
      "created" -> JsString(DateTimeConverter.convertToString(entity.created)),
      "updated" -> JsString(DateTimeConverter.convertToString(entity.updated)))

  private def userEntityDescriptionMap(entity: Entity): Map[String, JsValue] = Map(
      "id" -> JsString(entity.id.value.toString),
      "name" -> JsString(entity.name),
      "description" -> JsString(entity.description),
      "saved" -> JsBoolean(entity.saved))

  private def jsonDataObject(dataObject: DataObject) = dataObject match {
    case DataObjectReport(_) => JsObject() //TODO: fill when report is implemented
    case DataObjectReference(url) => JsObject(Map("url" -> JsString(url)))
  }
}
