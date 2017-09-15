/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.model.json.workflow

import spray.httpx.SprayJsonSupport
import spray.json._

import io.deepsense.deeplang.DOperable.AbstractMetadata

trait AbstractMetadataJsonProtocol extends DefaultJsonProtocol with SprayJsonSupport {
  implicit object AbstractMetadataFormat extends RootJsonFormat[AbstractMetadata] {
    override def write(am: AbstractMetadata): JsValue = am.serializeToJson
    override def read(value: JsValue): AbstractMetadata = ???
  }
}

object AbstractMetadataJsonProtocol extends AbstractMetadataJsonProtocol
