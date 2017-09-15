/**
 * Copyright 2015 deepsense.ai (CodiLime, Inc)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ai.deepsense.models.json.workflow

import ai.deepsense.commons.json.envelope.EnvelopeJsonWriter
import ai.deepsense.deeplang.DOperation
import ai.deepsense.deeplang.catalogs.doperations.DOperationDescriptor

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
