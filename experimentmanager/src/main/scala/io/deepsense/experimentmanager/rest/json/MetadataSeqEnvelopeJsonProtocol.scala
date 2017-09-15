/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.experimentmanager.rest.json

import spray.httpx.SprayJsonSupport
import spray.json._

import io.deepsense.commons.json.envelope.EnvelopeJsonWriter
import io.deepsense.deeplang.DOperable.AbstractMetadata

trait MetadataSeqEnvelopeJsonProtocol
  extends DefaultJsonProtocol
  with AbstractMetadataJsonProtocol
  with SprayJsonSupport {

  val metadataEnvelopeLabel = "metadata"
  implicit val operationsEnvelopeWriter =
    new EnvelopeJsonWriter[Seq[Option[AbstractMetadata]]](metadataEnvelopeLabel)
}
