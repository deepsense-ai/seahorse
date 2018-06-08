/**
 * Copyright 2015 deepsense.ai (CodiLime, Inc)
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

package ai.deepsense.deeplang.catalogs.doperations

import scala.reflect.runtime.universe.Type

import spray.json.JsValue
import ai.deepsense.deeplang.{DOperation, TypeUtils}
import ai.deepsense.deeplang.DPortPosition.DPortPosition
import ai.deepsense.deeplang.catalogs.SortPriority

/**
 * Represents a registered DOperation and stores its name and i/o port types.
 */
case class DOperationDescriptor(
    id: DOperation.Id,
    name: String,
    description: String,
    category: DOperationCategory,
    priority: SortPriority,
    hasDocumentation: Boolean,
    parametersJsonDescription: JsValue,
    inPorts: Seq[Type],
    inPortsLayout: Vector[DPortPosition],
    outPorts: Seq[Type],
    outPortsLayout: Vector[DPortPosition]) {

  override def toString: String = {
    def portsToString(ports: Seq[Type]): String = {
      ports.map(TypeUtils.typeToString).mkString(", ")
    }
    s"$name(${portsToString(inPorts)} => ${portsToString(outPorts)})"
  }
}
