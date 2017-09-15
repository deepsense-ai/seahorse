/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.experimentmanager.rest.json

import io.deepsense.commons.json.envelope.EnvelopeJsonWriter
import io.deepsense.deeplang.DOperable.AbstractMetadata
import spray.httpx.SprayJsonSupport
import spray.json._
import AbstractMetadataJsonProtocol._

trait MetadataSeqEnvelopeJsonProtocol extends DefaultJsonProtocol
    with SprayJsonSupport {
  val metadataEnvelopeLabel = "metadata"
  implicit val operationsEnvelopeWriter =
    new EnvelopeJsonWriter[Seq[Option[AbstractMetadata]]](metadataEnvelopeLabel)
}
