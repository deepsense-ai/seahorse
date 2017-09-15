/**
 * Copyright 2015, CodiLime Inc.
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

package io.deepsense.deeplang.catalogs.doperations

import scala.reflect.runtime.universe.Type

import io.deepsense.deeplang.parameters.ParametersSchema
import io.deepsense.deeplang.{DOperation, TypeUtils}

/**
 * Represents a registered DOperation and stores its name and i/o port types.
 */
case class DOperationDescriptor(
    id: DOperation.Id,
    name: String,
    version: String,
    description: String,
    category: DOperationCategory,
    parameters: ParametersSchema,
    inPorts: Seq[Type],
    outPorts: Seq[Type]) {

  override def toString: String = {
    def portsToString(ports: Seq[Type]): String = {
      ports.map(DOperationDescriptor.typeToString).mkString(", ")
    }
    s"$name(${portsToString(inPorts)} => ${portsToString(outPorts)})"
  }
}

object DOperationDescriptor {
  val typeSeparator = " with "

  def describeType(t: Type): Seq[String] = {
    t.toString.split(typeSeparator)
  }

  /** Helper method that converts scala types to readable strings. */
  private def typeToString(t: Type): String = {
    describeType(t).map(_.split("\\.").toList.last).mkString(typeSeparator)
  }
}
