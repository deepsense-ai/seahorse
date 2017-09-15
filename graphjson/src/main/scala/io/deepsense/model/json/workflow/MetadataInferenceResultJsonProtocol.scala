/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.model.json.workflow

import spray.httpx.SprayJsonSupport
import spray.json._

import io.deepsense.models.metadata.MetadataInferenceResult

trait MetadataInferenceResultJsonProtocol
    extends DefaultJsonProtocol
    with AbstractMetadataJsonProtocol
    with InferenceWarningsJsonProtocol
    with InferenceErrorJsonProtocol
    with SprayJsonSupport {

  implicit val metadataInferenceFormat = jsonFormat3(MetadataInferenceResult.apply)
}

object MetadataInferenceResultJsonProtocol extends MetadataInferenceResultJsonProtocol
