/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.workflowmanager.rest.json

import io.deepsense.workflowmanager.rest.metadata.MetadataInferenceResult
import spray.httpx.SprayJsonSupport
import spray.json._

trait MetadataInferenceResultJsonProtocol
    extends DefaultJsonProtocol
    with AbstractMetadataJsonProtocol
    with InferenceWarningsJsonProtocol
    with InferenceErrorJsonProtocol
    with SprayJsonSupport {

  implicit val metadataInferenceFormat = jsonFormat3(MetadataInferenceResult.apply)
}

object MetadataInferenceResultJsonProtocol extends MetadataInferenceResultJsonProtocol
