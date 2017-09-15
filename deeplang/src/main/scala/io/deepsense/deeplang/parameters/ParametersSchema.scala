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

import scala.collection.immutable.ListMap

import spray.json._

import io.deepsense.deeplang.exceptions.DeepLangException
import io.deepsense.deeplang.parameters.ParameterConversions._
import io.deepsense.deeplang.parameters.exceptions.NoSuchParameterException

/**
 * Schema for a given set of DOperation parameters
 * Holds Parameters that are passed to DOperation.
 */
class ParametersSchema protected (private val schemaMap: ListMap[String, Parameter] = ListMap.empty)
  extends Serializable {

  // TODO: Parameter name should be taken from parameter, not from schema.
  // TODO: When it's there, this method should be simplified to schema.values.foreach(_.validate)
  def validate: Vector[DeepLangException] = schemaMap.flatMap {
    case (name: String, parameter: Parameter) => parameter.validate(name)
  }.toVector

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
   * Substitutes placeholders with concrete variable values.
   */
  def substitutePlaceholders(variables: Map[String, String]): Unit =
    schemaMap.foreach { case (name, holder) => holder.substitutePlaceholders(variables) }

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

  def getBoolean(name: String): Boolean = getBooleanParameter(name).value

  def getString(name: String): String = getStringParameter(name).value

  def getDouble(name: String): Double = getNumericParameter(name).value

  def getChoice(name: String): Selection = getChoiceParameter(name).selection

  def getMultipleChoice(name: String): Traversable[Selection] = {
    getMultipleChoiceParameter(name).selections
  }

  def getMultiplicatedSchema(name: String): Vector[ParametersSchema] = {
    getParametersSequence(name).value
  }

  def getSingleColumnSelection(name: String): SingleColumnSelection = {
    getSingleColumnSelectorParameter(name).value
  }

  def getColumnSelection(name: String): MultipleColumnSelection = {
    getColumnSelectorParameter(name).value
  }

  def getNewColumnName(name: String): String = {
    getSingleColumnCreatorParameter(name).value
  }

  def getNewColumnNames(name: String): Vector[String] = {
    getMultipleColumnCreatorParameter(name).value
  }

  def getNewColumnsPrefix(name: String): String = {
    getPrefixBasedColumnCreatorParameter(name).value
  }

  def ++(other: ParametersSchema): ParametersSchema = {
    new ParametersSchema(schemaMap ++ other.schemaMap)
  }
}

object ParametersSchema {
  def apply(args: (String, Parameter)*): ParametersSchema = new ParametersSchema(ListMap(args: _*))
}
