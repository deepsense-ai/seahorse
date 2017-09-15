/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.experimentmanager.rest.json

import io.deepsense.deeplang.DOperable.AbstractMetadata
import spray.httpx.SprayJsonSupport
import spray.json._

trait AbstractMetadataJsonProtocol extends DefaultJsonProtocol with SprayJsonSupport {
  implicit object AbstractMetadataFormat extends RootJsonFormat[AbstractMetadata] {
    override def write(am: AbstractMetadata): JsValue = am.serializeToJson
    override def read(value: JsValue): AbstractMetadata = ???
  }
}

object AbstractMetadataJsonProtocol extends AbstractMetadataJsonProtocol
