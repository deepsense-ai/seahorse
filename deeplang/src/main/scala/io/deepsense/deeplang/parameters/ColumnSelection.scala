/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Witold Jedrzejewski
 */

package io.deepsense.deeplang.parameters

import spray.json._

import io.deepsense.deeplang.parameters.ColumnRole._
import io.deepsense.deeplang.parameters.ColumnType._

/**
 * Represents selecting subset of columns of dataframe.
 */
@SerialVersionUID(1)
sealed abstract class ColumnSelection(
    typeName: String)
  extends Serializable
  with DefaultJsonProtocol {

  final def toJson: JsValue = JsObject(
    ColumnSelection.typeField -> typeName.toJson,
    ColumnSelection.valuesField -> valuesToJson)

  protected def valuesToJson: JsValue
}

object ColumnSelection extends Serializable {
  val typeField = "type"

  val valuesField = "values"

  def fromJson(jsValue: JsValue): ColumnSelection = jsValue match {
    case JsObject(map) =>
      val value = map(valuesField)
      map(typeField) match {
        case JsString(NameColumnSelection.typeName) =>
          NameColumnSelection.fromJson(value)
        case JsString(IndexColumnSelection.typeName) =>
          IndexColumnSelection.fromJson(value)
        case JsString(RoleColumnSelection.typeName) =>
          RoleColumnSelection.fromJson(value)
        case JsString(TypeColumnSelection.typeName) =>
          TypeColumnSelection.fromJson(value)
        case unknownType =>
          throw new DeserializationException(s"Cannot create column selection with $jsValue:" +
            s"unknown type $unknownType")
      }
    case _ => throw new DeserializationException(
      s"Cannot create column selection with $jsValue: object expected.")
  }
}

/**
 * Represents selecting subset of columns which have one of given names.
 */
case class NameColumnSelection(names: List[String])
  extends ColumnSelection(NameColumnSelection.typeName) {

  override protected def valuesToJson: JsValue = names.toJson
}

object NameColumnSelection {
  val typeName = "columnList"

  def fromJson(jsValue: JsValue): NameColumnSelection = {
    import DefaultJsonProtocol._
    NameColumnSelection(jsValue.convertTo[List[String]])
  }
}
/**
 * Represents selecting subset of columns which have one of given indexes.
 */
case class IndexColumnSelection(indexes: List[Int])
  extends ColumnSelection(IndexColumnSelection.typeName) {

  override protected def valuesToJson: JsValue = indexes.toJson
}

object IndexColumnSelection {
  val typeName = "indexList"

  def fromJson(jsValue: JsValue): IndexColumnSelection = {
    import DefaultJsonProtocol._
    IndexColumnSelection(jsValue.convertTo[List[Int]])
  }
}

/**
 * Represents selecting subset of columns which have one of given roles.
 */
case class RoleColumnSelection(roles: List[ColumnRole])
  extends ColumnSelection(RoleColumnSelection.typeName) {

  override protected def valuesToJson: JsValue = roles.map(_.toString).toJson
}

object RoleColumnSelection {
  val typeName = "roleList"

  def fromJson(jsValue: JsValue): RoleColumnSelection = {
    import DefaultJsonProtocol._
    RoleColumnSelection(jsValue.convertTo[List[String]].map(ColumnRole.withName))
  }
}

/**
 * Represents selecting subset of columns which have one of given types.
 */
case class TypeColumnSelection(types: List[ColumnType])
  extends ColumnSelection(TypeColumnSelection.typeName) {

  override protected def valuesToJson: JsValue = types.map(_.toString).toJson

}

object TypeColumnSelection {
  val typeName = "typeList"

  def fromJson(jsValue: JsValue): TypeColumnSelection = {
    import DefaultJsonProtocol._
    TypeColumnSelection(jsValue.convertTo[List[String]].map(ColumnType.withName))
  }
}
