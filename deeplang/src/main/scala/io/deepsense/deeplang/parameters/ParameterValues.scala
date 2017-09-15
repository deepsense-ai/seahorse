/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Radoslaw Kotowski
 *
 * This is a place for all types of values that can be put in Parameters.
 */

package io.deepsense.deeplang.parameters

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
 * Represents list of schemas that all conform to some predefined schema,
 * but each one can be filled with different value.
 */
case class Multiplied(schemas: Vector[ParametersSchema])

/**
 * Represents selecting single column of dataframe.
 */
sealed abstract class SingleColumnSelection(typeName: String) extends DefaultJsonProtocol {
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
    import DefaultJsonProtocol._
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
    import DefaultJsonProtocol._
    NameSingleColumnSelection(jsValue.convertTo[String])
  }
}

/**
 * Represents selecting subset of columns of dataframe. It consists of a few column selections,
 * each of which can select columns in different way (by indexes, by names etc.).
 * Subset selected by this class can be considered as sum of subsets selected by 'selections'.
 * @param selections list of selections
 */
case class MultipleColumnSelection(selections: Vector[ColumnSelection])

object MultipleColumnSelection {
  def fromJson(jsValue: JsValue): MultipleColumnSelection = jsValue match {
    case JsArray(x) => MultipleColumnSelection(x.map(ColumnSelection.fromJson))
    case _ => throw new DeserializationException(s"Cannot create multiple column selection " +
      s"from $jsValue - array expected.")
  }
}
