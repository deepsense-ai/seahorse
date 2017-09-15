/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Witold Jedrzejewski
 */

package io.deepsense.deeplang.catalogs.doperations

import java.util.UUID

import scala.reflect.runtime.universe.Type

import io.deepsense.deeplang.parameters.ParametersSchema
import io.deepsense.deeplang.TypeUtils

/**
 * Represents a registered DOperation and stores its name and i/o port types.
 */
case class DOperationDescriptor(
    id: UUID,
    name: String,
    description: String,
    category: DOperationCategory,
    parameters: ParametersSchema,
    inPorts: Seq[Type],
    outPorts: Seq[Type]) {

  override def toString: String = {
    def portsToString(ports: Seq[Type]) = {
      ports.map(DOperationDescriptor.typeToString).mkString(", ")
    }
    s"$name(${portsToString(inPorts)} => ${portsToString(outPorts)})"
  }
}

object DOperationDescriptor {
  val typeSeparator = " with "

  def describeType(t: Type): Seq[String] = {
    t.toString.split(typeSeparator).map(TypeUtils.shortNameOfType)
  }

  /** Helper method that converts scala types to readable strings. */
  private def typeToString(t: Type): String = {
    describeType(t).map(_.split("\\.").toList.last).mkString(typeSeparator)
  }
}
