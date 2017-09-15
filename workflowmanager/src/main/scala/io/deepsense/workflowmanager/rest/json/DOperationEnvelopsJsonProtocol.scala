/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.workflowmanager.rest.json

import io.deepsense.commons.json.envelope.EnvelopeJsonWriter
import io.deepsense.deeplang.DOperation
import io.deepsense.deeplang.catalogs.doperations.DOperationDescriptor

trait DOperationsEnvelopeJsonProtocol extends DOperationDescriptorJsonProtocol {
  val operationsEnvelopeLabel = "operations"
  private implicit val operationsFormat = DOperationDescriptorBaseFormat
  implicit val operationsEnvelopeWriter =
    new EnvelopeJsonWriter[Map[DOperation.Id, DOperationDescriptor]](operationsEnvelopeLabel)
}

trait DOperationEnvelopeJsonProtocol extends DOperationDescriptorJsonProtocol {
  private implicit val operationFormat = DOperationDescriptorFullFormat
  val operationEnvelopeLabel = "operation"
  implicit val operationEnvelopeWriter =
    new EnvelopeJsonWriter[DOperationDescriptor](operationEnvelopeLabel)
}

trait DOperationEnvelopesJsonProtocol
  extends DOperationsEnvelopeJsonProtocol
  with DOperationEnvelopeJsonProtocol

object DOperationEnvelopesJsonProtocol extends DOperationEnvelopesJsonProtocol
