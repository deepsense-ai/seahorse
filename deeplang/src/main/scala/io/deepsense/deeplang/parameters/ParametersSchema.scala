/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.parameters

import scala.collection.immutable.ListMap

import spray.json._

import io.deepsense.deeplang.parameters.ParameterConversions._
import io.deepsense.deeplang.parameters.exceptions.NoSuchParameterException

/**
 * Schema for a given set of DOperation parameters
 * Holds Parameters that are passed to DOperation.
 */
class ParametersSchema protected (private val schemaMap: ListMap[String, Parameter] = ListMap.empty)
  extends Serializable {

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

  override def equals(other: Any): Boolean = other match {
    case that: ParametersSchema => schemaMap == that.schemaMap
    case _ => false
  }

  override def hashCode: Int = schemaMap.hashCode()

  /**
   * Tells if the schema does not contain any parameters.
   */
  def isEmpty: Boolean = schemaMap.isEmpty

  /**
   * Json representation describing parameters of this schema.
   */
  def toJson: JsValue = JsArray(schemaMap.map { case (name, parameter) =>
    JsObject(parameter.jsDescription + ("name" -> JsString(name)))
  }.toVector)

  /**
   * Json representation of values held by this schema's parameters.
   */
  def valueToJson: JsValue = JsObject(schemaMap.mapValues(_.valueToJson))

  def fillValuesWithJson(jsValue: JsValue): Unit = jsValue match {
    case JsObject(map) =>
      for ((label, value) <- map) {
        schemaMap.get(label) match {
          case Some(parameter) => parameter.fillValueWithJson(value)
          case None => throw new DeserializationException(s"Cannot fill parameters schema with " +
            s"$jsValue: unknown parameter label $label.")
        }
      }
    case JsNull => // JsNull is treated as empty object
    case _ => throw new DeserializationException(s"Cannot fill parameters schema with $jsValue:" +
      s"object expected.")
  }

  def getBooleanParameter(name: String): BooleanParameter = get[BooleanParameter](name)

  def getStringParameter(name: String): StringParameter = get[StringParameter](name)

  def getNumericParameter(name: String): NumericParameter = get[NumericParameter](name)

  def getChoiceParameter(name: String): ChoiceParameter = get[ChoiceParameter](name)

  def getMultipleChoiceParameter(name: String): MultipleChoiceParameter = {
    get[MultipleChoiceParameter](name)
  }

  def getParametersSequence(name: String): ParametersSequence = {
    get[ParametersSequence](name)
  }

  def getSingleColumnSelectorParameter(name: String): SingleColumnSelectorParameter = {
    get[SingleColumnSelectorParameter](name)
  }

  def getColumnSelectorParameter(name: String): ColumnSelectorParameter = {
    get[ColumnSelectorParameter](name)
  }

  def getSingleColumnCreatorParameter(name: String): SingleColumnCreatorParameter = {
    get[SingleColumnCreatorParameter](name)
  }

  def getMultipleColumnCreatorParameter(name: String): MultipleColumnCreatorParameter = {
    get[MultipleColumnCreatorParameter](name)
  }

  def getPrefixBasedColumnCreatorParameter(name: String): PrefixBasedColumnCreatorParameter = {
    get[PrefixBasedColumnCreatorParameter](name)
  }

  def getBoolean(name: String): Option[Boolean] = getBooleanParameter(name).value

  def getString(name: String): Option[String] = getStringParameter(name).value

  def getDouble(name: String): Option[Double] = getNumericParameter(name).value

  def getChoice(name: String): Option[Selection] = getChoiceParameter(name).selection

  def getMultipleChoice(name: String): Option[Traversable[Selection]] = {
    getMultipleChoiceParameter(name).selections
  }

  def getMultiplicatedSchema(name: String): Option[Vector[ParametersSchema]] = {
    getParametersSequence(name).value
  }

  def getSingleColumnSelection(name: String): Option[SingleColumnSelection] = {
    getSingleColumnSelectorParameter(name).value
  }

  def getColumnSelection(name: String): Option[MultipleColumnSelection] = {
    getColumnSelectorParameter(name).value
  }

  def getNewColumnName(name: String): Option[String] = {
    getSingleColumnCreatorParameter(name).value
  }

  def getNewColumnNames(name: String): Option[Vector[String]] = {
    getMultipleColumnCreatorParameter(name).value
  }

  def getNewColumnsPrefix(name: String): Option[String] = {
    getPrefixBasedColumnCreatorParameter(name).value
  }

  def ++(other: ParametersSchema): ParametersSchema = {
    new ParametersSchema(schemaMap ++ other.schemaMap)
  }
}

object ParametersSchema {
  def apply(args: (String, Parameter)*): ParametersSchema = new ParametersSchema(ListMap(args: _*))
}
