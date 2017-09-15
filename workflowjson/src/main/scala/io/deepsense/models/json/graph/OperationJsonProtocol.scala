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

package io.deepsense.models.json.graph

import spray.json._

import io.deepsense.commons.json.IdJsonProtocol
import io.deepsense.deeplang.DOperation
import io.deepsense.deeplang.catalogs.doperations.DOperationsCatalog

object OperationJsonProtocol extends IdJsonProtocol {

  val Operation = "operation"
  val Version = "version"
  val Name = "name"
  val Id = "id"
  val Parameters = "parameters"

  implicit object DOperationWriter
    extends JsonWriter[DOperation]
    with DefaultJsonProtocol
    with IdJsonProtocol {

    override def write(operation: DOperation): JsValue = {
      JsObject(
        Operation -> JsObject(
          Id -> operation.id.toJson,
          Name -> operation.name.toJson,
          Version -> operation.version.toJson),
        Parameters -> operation.parameters.valueToJson)
    }
  }

  class DOperationReader(catalog: DOperationsCatalog)
    extends JsonReader[DOperation]
    with DefaultJsonProtocol {
    override def read(json: JsValue): DOperation = json match {
      case JsObject(fields) =>
        val operationJs = fields(Operation).asJsObject
        val operation = catalog
          .createDOperation(operationJs.fields(Id).convertTo[DOperation.Id])
        operation.parameters.fillValuesWithJson(fields(Parameters))
        operation
      case x =>
        throw new DeserializationException(s"Expected JsObject with a node but got $x")
    }
  }
}
