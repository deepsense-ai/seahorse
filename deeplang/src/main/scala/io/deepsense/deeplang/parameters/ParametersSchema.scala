/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Witold Jedrzejewski
 */

package io.deepsense.deeplang.parameters

import spray.json.{JsValue, JsObject}

import io.deepsense.deeplang.parameters.ParameterConversions._
import io.deepsense.deeplang.parameters.exceptions.NoSuchParameterException

/**
 * Schema for a given set of DOperation parameters
 * Holds Parameters that are passed to DOperation.
 */
class ParametersSchema protected (schemaMap: Map[String, Parameter] = Map.empty) {
  def validate: Unit = schemaMap.values.foreach(_.validate)

  private def get[T <: Parameter](name: String)(implicit converter: ParameterConverter[T]): T = {
    schemaMap.get(name) match {
      case Some(parameterHolder) => converter.convert(parameterHolder)
      case None => throw NoSuchParameterException(name)
    }
  }

  /**
   * Creates another schema with the same keys and parameter holders under them.
   * Values held by holders won't be copied.
   */
  private[parameters] def replicate: ParametersSchema = {
    val replicatedSchemaMap = for ((name, holder) <- schemaMap) yield (name, holder.replicate)
    new ParametersSchema(replicatedSchemaMap)
  }

  /**
   * Tells if this schema does not contain any parameters.
   */
  def isEmpty: Boolean = schemaMap.isEmpty

  /**
   * Json representation describing parameters of this schema.
   */
  def toJson: JsValue = JsObject(schemaMap.mapValues(_.toJson))

  /**
   * Json representation of values held by this schema's parameters.
   */
  def valueToJson: JsValue = JsObject(schemaMap.mapValues(_.valueToJson))

  def getBooleanParameter(name: String): BooleanParameter = get[BooleanParameter](name)

  def getStringParameter(name: String): StringParameter = get[StringParameter](name)

  def getNumericParameter(name: String): NumericParameter = get[NumericParameter](name)

  def getChoiceParameter(name: String): ChoiceParameter = get[ChoiceParameter](name)

  def getMultipleChoiceParameter(name: String): MultipleChoiceParameter = {
    get[MultipleChoiceParameter](name)
  }

  def getMultiplicatedParameter(name: String): MultiplierParameter = {
    get[MultiplierParameter](name)
  }

  def getSingleColumnSelectorParameter(name: String): SingleColumnSelectorParameter = {
    get[SingleColumnSelectorParameter](name)
  }

  def getColumnSelectorParameter(name: String): ColumnSelectorParameter = {
    get[ColumnSelectorParameter](name)
  }

  def getBoolean(name: String): Option[Boolean] = getBooleanParameter(name).value

  def getString(name: String): Option[String] = getStringParameter(name).value

  def getDouble(name: String): Option[Double] = getNumericParameter(name).value

  def getChoice(name: String): Option[Selection] = getChoiceParameter(name).value

  def getMultipleChoice(name: String): Option[MultipleSelection] = {
    getMultipleChoiceParameter(name).value
  }

  def getMultiplicated(name: String): Option[Multiplied] = {
    getMultiplicatedParameter(name).value
  }

  def getSingleColumnSelection(name: String): Option[SingleColumnSelection] = {
    getSingleColumnSelectorParameter(name).value
  }

  def getColumnSelection(name: String): Option[MultipleColumnSelection] = {
    getColumnSelectorParameter(name).value
  }
}

object ParametersSchema {
  def apply(args: (String, Parameter)*) = new ParametersSchema(Map(args: _*))
}
