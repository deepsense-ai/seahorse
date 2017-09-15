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
case class Multiplied(schemas: List[ParametersSchema])

/**
 * Represents selecting single column of dataframe.
 */
sealed abstract class SingleColumnSelection(typeName: String) extends DefaultJsonProtocol {
  final def toJson: JsValue = {
    JsObject("type" -> typeName.toJson, "value" -> valueToJson)
  }

  protected def valueToJson: JsValue
}

/**
 * Points to column of dataframe with given index.
 * @param value index of chosen column
 */
case class IndexSingleColumnSelection(value: Int) extends SingleColumnSelection("index") {
  override protected def valueToJson: JsValue = value.toJson
}

/**
 * Points to column of dataframe with given name.
 * @param value name of chosen column
 */
case class NameSingleColumnSelection(value: String) extends SingleColumnSelection("column") {
  override protected def valueToJson: JsValue = value.toJson
}

/**
 * Represents selecting subset of columns of dataframe. It consists of a few column selections,
 * each of which can select columns in different way (by indexes, by names etc.).
 * Subset selected by this class can be considered as sum of subsets selected by 'selections'.
 * @param selections list of selections
 */
case class MultipleColumnSelection(selections: List[ColumnSelection])
