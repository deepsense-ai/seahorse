/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.graphjson

import java.util.UUID

import spray.json._

import io.deepsense.graph.Node
import io.deepsense.graph.Node.Id

trait NodeJsonProtocol extends DefaultJsonProtocol {

  import OperationJsonProtocol.DOperationWriter

  implicit object NodeIdFormat extends JsonFormat[Node.Id] {
    override def read(json: JsValue): Id = json match {
      case JsString(value) => Id(UUID.fromString(value))
      case x =>
        throw new DeserializationException(s"Could not read Node.Id! Expected JsString but was $x")
    }

    override def write(obj: Id): JsValue = obj.value.toString.toJson
  }

  implicit object NodeWriter extends JsonWriter[Node] {
    override def write(node: Node): JsValue = JsObject(
      Map(NodeJsonProtocol.Id -> node.id.toJson) ++
        node.operation.toJson.asJsObject.fields)
  }
}

object NodeJsonProtocol extends NodeJsonProtocol {
  val Id = "id"
}
