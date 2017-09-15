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

package ai.deepsense.models.json.graph

import ai.deepsense.commons.json.IdJsonProtocol
import ai.deepsense.deeplang.DOperation
import ai.deepsense.deeplang.catalogs.doperations.DOperationsCatalog
import ai.deepsense.deeplang.catalogs.doperations.exceptions.DOperationNotFoundException
import spray.json._

import ai.deepsense.models.json.graph.GraphJsonProtocol.GraphReader

object OperationJsonProtocol extends IdJsonProtocol {

  val Operation = "operation"
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
          Name -> operation.name.toJson),
        Parameters -> operation.paramValuesToJson)
    }
  }

  class DOperationReader(graphReader: GraphReader)
    extends JsonReader[DOperation]
    with DefaultJsonProtocol {
    override def read(json: JsValue): DOperation = json match {
      case JsObject(fields) =>
        val operationJs = fields(Operation).asJsObject
        val operationId = operationJs.fields
          .getOrElse(Id, deserializationError(s"Operation id field '$Id' is missing"))
          .convertTo[DOperation.Id]

        val operation = try {
          graphReader.catalog.createDOperation(operationId)
        } catch {
          case notFound: DOperationNotFoundException =>
            deserializationError(
              s"DOperation with id = '${notFound.operationId}' does not exist",
              notFound)
        }

        val parameters = fields.getOrElse(Parameters,
          deserializationError(s"Operation parameters field '$Parameters' is missing"))

        operation.setParamsFromJson(parameters, graphReader)
        operation
      case x =>
        throw new DeserializationException(s"Expected JsObject with a node but got $x")
    }
  }
}
