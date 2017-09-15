/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Witold Jedrzejewski
 */

package io.deepsense.deeplang.parameters

import io.deepsense.deeplang.parameters.ParameterConversions._
import io.deepsense.deeplang.parameters.exceptions.NoSuchParameterException

/**
 * Schema for a given set of DOperation parameters
 * Holds Parameters that are passed to DOperation.
 */
class ParametersSchema protected (schemaMap: Map[String, ParameterHolder] = Map.empty) {
  def validate: Unit = schemaMap.values.foreach(_.validate)

  private def get[T](name: String)(implicit converter: ParameterConverter[T]): Option[T] = {
    schemaMap.get(name) match {
      case Some(parameterHolder) => converter.convert(parameterHolder.value)
      case None => throw NoSuchParameterException(name)
    }
  }

  /**
   * Returns parameter holder that is assigned to given label.
   */
  def apply(label: String): ParameterHolder = schemaMap(label)

  /**
   * Creates another schema with the same keys and parameter holders under them.
   * Values held by holders won't be copied.
   */
  private[parameters] def replicate: ParametersSchema = {
    val replicatedSchemaMap = for ((name, holder) <- schemaMap) yield (name, holder.replicate)
    new ParametersSchema(replicatedSchemaMap)
  }

  def getBoolean(name: String): Option[Boolean] = get[Boolean](name)

  def getString(name: String): Option[String] = get[String](name)

  def getDouble(name: String): Option[Double] = get[Double](name)

  def getChoice(name: String): Option[ChoiceParameter] = get[ChoiceParameter](name)

  def getMultipleChoice(name: String): Option[MultipleChoiceParameter] = {
    get[MultipleChoiceParameter](name)
  }

  def getMultiplicator(name: String): Option[MultiplicatorParameter] = {
    get[MultiplicatorParameter](name)
  }

  def getSingleColumnSelection(name: String): Option[SingleColumnSelection] = {
    get[SingleColumnSelection](name)
  }

  def getColumnSelection(name: String): Option[MultipleColumnSelection] = {
    get[MultipleColumnSelection](name)
  }
}

object ParametersSchema {
  def apply(args: (String, ParameterHolder)*) = new ParametersSchema(Map(args: _*))
}
