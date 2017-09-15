/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.model.json.graph

import spray.json._

import io.deepsense.deeplang.{DKnowledge, DOperable}

trait GraphKnowledgeJsonProtocol extends DefaultJsonProtocol {

  implicit object DKnowledgeJsonFormat
    extends JsonFormat[DKnowledge[DOperable]]
    with DefaultJsonProtocol {

    override def write(dKnowledge: DKnowledge[DOperable]): JsValue =
      JsArray(dKnowledge.types.map(_.getClass.getName.toJson).toVector)

    override def read(json: JsValue): DKnowledge[DOperable] = ???
  }
}

object GraphKnowledgeJsonProtocol extends GraphKnowledgeJsonProtocol
