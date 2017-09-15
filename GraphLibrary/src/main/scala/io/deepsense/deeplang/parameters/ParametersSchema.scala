/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Witold Jedrzejewski
 */

package io.deepsense.deeplang.parameters

import ParameterConversions._
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

  def getBooleanParameter(name: String): Option[BooleanParameter] = get[BooleanParameter](name)

  def getStringParameter(name: String): Option[StringParameter] = get[StringParameter](name)

  def getNumericParameter(name: String): Option[NumericParameter] = get[NumericParameter](name)
  // TODO: add get method for other parameter types
}

object ParametersSchema {
  def apply(args: (String, ParameterHolder)*) = new ParametersSchema(Map(args: _*))
}
