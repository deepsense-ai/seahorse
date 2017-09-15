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
sealed abstract class ColumnSelection(typeName: String) extends DefaultJsonProtocol {
  final def toJson: JsValue = JsObject("type" -> typeName.toJson, "values" -> valuesToJson)

  protected def valuesToJson: JsValue
}

/**
 * Represents selecting subset of columns which have one of given names.
 */
case class NameColumnSelection(names: List[String]) extends ColumnSelection("columnList") {
  override protected def valuesToJson: JsValue = names.toJson
}

/**
 * Represents selecting subset of columns which have one of given indexes.
 */
case class IndexColumnSelection(indexes: List[Int]) extends ColumnSelection("indexList") {
  override protected def valuesToJson: JsValue = indexes.toJson
}

/**
 * Represents selecting subset of columns which have one of given roles.
 */
case class RoleColumnSelection(roles: List[ColumnRole]) extends ColumnSelection("roleList") {
  override protected def valuesToJson: JsValue = roles.map(_.toString).toJson
}

/**
 * Represents selecting subset of columns which have one of given types.
 */
case class TypeColumnSelection(types: List[ColumnType]) extends ColumnSelection("typeList") {
  override protected def valuesToJson: JsValue = types.map(_.toString).toJson
}
