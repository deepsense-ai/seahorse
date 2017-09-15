/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.experimentmanager.rest.json

import io.deepsense.deeplang.DOperable.AbstractMetadata
import spray.httpx.SprayJsonSupport
import spray.json._

trait AbstractMetadataJsonProtocol extends DefaultJsonProtocol with SprayJsonSupport {
  implicit object CategoriesMappingFormat extends RootJsonFormat[AbstractMetadata] {
    override def write(cm: AbstractMetadata): JsValue =
      cm.accept(new MetadataJsonSerializingVisitor)
    override def read(value: JsValue): AbstractMetadata =
      throw new RuntimeException("Not supported")
  }
}

object AbstractMetadataJsonProtocol extends AbstractMetadataJsonProtocol
