/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.graphjson

import spray.json._

import io.deepsense.commons.json.IdJsonProtocol
import io.deepsense.graph.Node

trait NodeJsonProtocol extends DefaultJsonProtocol with IdJsonProtocol {

  import OperationJsonProtocol.DOperationWriter

  implicit object NodeWriter extends JsonWriter[Node] {
    override def write(node: Node): JsValue = JsObject(
      Map(NodeJsonProtocol.Id -> node.id.toJson) ++
        node.operation.toJson.asJsObject.fields)
  }
}

object NodeJsonProtocol extends NodeJsonProtocol {
  val Id = "id"
}
