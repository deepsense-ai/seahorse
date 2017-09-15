/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.workflowmanager.rest.protocols

import org.joda.time.DateTime

import io.deepsense.commons.json.DateTimeJsonProtocol
import io.deepsense.commons.json.envelope.EnvelopeJsonWriter


trait ResultsUploadTimeJsonProtocol {
  val envelopeLabel = "resultsUploadTime"
  private implicit val dateTimeFormat = DateTimeJsonProtocol.DateTimeJsonFormat
  implicit val lastExecutionWriter =
    new EnvelopeJsonWriter[DateTime](envelopeLabel)
}

object ResultsUploadTimeJsonProtocol extends ResultsUploadTimeJsonProtocol
