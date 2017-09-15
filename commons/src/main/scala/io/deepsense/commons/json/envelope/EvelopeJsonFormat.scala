/**
 * Copyright (c) 2015, CodiLime, Inc.
 */

package io.deepsense.commons.json.envelope

import spray.json._

/**
 * JSON Encoder and decoder of Envelope[T] objects
 */
class EnvelopeJsonFormat[T : JsonFormat](private val label: String)
  extends RootJsonFormat[Envelope[T]] {
  override def write(e: Envelope[T]) = JsObject(label -> e.content.toJson)
  override def read(json: JsValue): Envelope[T] = json match {
    case JsObject(fields) if (fields.size == 1 && fields.contains(label)) =>
      Envelope(fields.head._2.convertTo[T])
    case _ => deserializationError(s"Expected envelope with '$label' label, but got $json")
  }
 }
