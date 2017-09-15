/**
 * Copyright 2015, deepsense.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.deepsense.deeplang.parameters

import spray.json.DefaultJsonProtocol._
import spray.json._

/**
 * Represents selecting one of variety of options.
 * @param label name denoting selected option
 * @param selectedSchema parameters schema bound to selected option
 */
case class Selection(label: String, selectedSchema: ParametersSchema)

/**
 * Represents selecting subset of variety of options.
 */
case class MultipleSelection(choices: Traversable[Selection])

/**
 * Represents selecting single column of dataframe.
 */
@SerialVersionUID(1)
sealed abstract class SingleColumnSelection(
    typeName: String)
  extends Serializable {

  final def toJson: JsValue = {
    JsObject(
      SingleColumnSelection.typeField -> JsString(typeName),
      SingleColumnSelection.valueField -> valueToJson)
  }

  protected def valueToJson: JsValue
}

object SingleColumnSelection {
  val typeField = "type"

  val valueField = "value"

  def fromJson(jsValue: JsValue): SingleColumnSelection = jsValue match {
    case JsObject(map) =>
      val value = map(valueField)
      map(typeField) match {
        case JsString(IndexSingleColumnSelection.typeName) =>
          IndexSingleColumnSelection.fromJson(value)
        case JsString(NameSingleColumnSelection.typeName) =>
          NameSingleColumnSelection.fromJson(value)
        case unknownType =>
          throw new DeserializationException(s"Cannot create single column selection with " +
            s"$jsValue: unknown selection type $unknownType.")
      }
    case _ =>
      throw new DeserializationException(s"Cannot create single column selection with $jsValue:" +
        s"object expected.")
  }
}

/**
 * Points to column of dataframe with given index.
 * @param value index of chosen column
 */
case class IndexSingleColumnSelection(value: Int)
  extends SingleColumnSelection(IndexSingleColumnSelection.typeName) {

  override protected def valueToJson: JsValue = value.toJson
}

object IndexSingleColumnSelection {
  val typeName = "index"

  def fromJson(jsValue: JsValue): IndexSingleColumnSelection = {
    IndexSingleColumnSelection(jsValue.convertTo[Int])
  }
}

/**
 * Points to column of dataframe with given name.
 * @param value name of chosen column
 */
case class NameSingleColumnSelection(value: String)
  extends SingleColumnSelection(NameSingleColumnSelection.typeName) {

  override protected def valueToJson: JsValue = value.toJson
}

object NameSingleColumnSelection {
  val typeName = "column"

  def fromJson(jsValue: JsValue): NameSingleColumnSelection = {
    NameSingleColumnSelection(jsValue.convertTo[String])
  }
}

/**
 * Represents selecting subset of columns of dataframe. It consists of a few column selections,
 * each of which can select columns in different way (by indexes, by names etc.).
 * Subset selected by this class can be considered as sum of subsets selected by 'selections'.
 * @param selections list of selections
 * @param excluding whether list of selections is excluding
 */
case class MultipleColumnSelection(selections: Vector[ColumnSelection],
    excluding: Boolean = false) {
  def validate: Unit = selections.foreach(_.validate)
}

object MultipleColumnSelection {

  val selectionsField = "selections"

  val excludingField = "excluding"

  def fromJson(jsValue: JsValue): MultipleColumnSelection = jsValue match {
    case JsObject(map) =>
      (map(selectionsField), map(excludingField)) match {
        case (JsArray(x), JsBoolean(excluding)) =>
          MultipleColumnSelection(x.map(ColumnSelection.fromJson), excluding)
        case _ => throw new DeserializationException(s"Cannot create multiple column selection " +
          s"from $jsValue.")
      }
    case _ => throw new DeserializationException(s"Cannot create multiple column selection " +
      s"from $jsValue.")
  }
}
