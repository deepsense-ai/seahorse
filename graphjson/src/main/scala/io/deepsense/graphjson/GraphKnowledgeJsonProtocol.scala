/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Wojciech Jurczyk
 */

package io.deepsense.graphjson

import spray.json._

import io.deepsense.deeplang.{DKnowledge, DOperable}

trait GraphKnowledgeJsonProtocol {

  implicit object DKnowledgeJsonWriter
    extends JsonWriter[DKnowledge[DOperable]]
    with DefaultJsonProtocol {

    override def write(dKnowledge: DKnowledge[DOperable]): JsValue =
      JsArray(dKnowledge.types.map(_.getClass.getName.toJson).toVector)
  }

  implicit object DKnowledgeVectorJsonWriter
    extends JsonWriter[Vector[DKnowledge[DOperable]]]
    with DefaultJsonProtocol {

    override def write(dKnowledges: Vector[DKnowledge[DOperable]]): JsValue = {
      JsArray(dKnowledges.map(_.toJson))
    }
  }
}

object GraphKnowledgeJsonProtocol extends GraphKnowledgeJsonProtocol
