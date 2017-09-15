/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.experimentmanager.rest.json

import io.deepsense.deeplang.DOperable.MetadataVisitor
import io.deepsense.deeplang.doperables.dataframe.DataFrameMetadata
import io.deepsense.experimentmanager.rest.json.DataFrameMetadataJsonProtocol._
import spray.json._

/**
 * MetadataJsonSerializingVisitor keeps the logic of serialization away from
 * the Metadata hierarchy and provides type wrapping:
 * { "type" -> metadataClassName, "content" -> metadataObject }
 */
class MetadataJsonSerializingVisitor extends MetadataVisitor[JsValue] {
  override def visit(dataFrameMetadata: DataFrameMetadata): JsValue =
    wrap(dataFrameMetadata.getClass.getSimpleName, dataFrameMetadata.toJson)
  private def wrap(metadataType: String, content: JsValue): JsValue =
    JsObject("type" -> JsString(metadataType), "content" -> content)
}

object MetadataJsonSerializingVisitor extends MetadataJsonSerializingVisitor
