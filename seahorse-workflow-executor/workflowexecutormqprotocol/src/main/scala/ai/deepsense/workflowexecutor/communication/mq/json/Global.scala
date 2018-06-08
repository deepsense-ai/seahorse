/**
 * Copyright 2016 deepsense.ai (CodiLime, Inc)
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

package ai.deepsense.workflowexecutor.communication.mq.json

import java.nio.charset.Charset

import ai.deepsense.deeplang.CatalogRecorder
import ai.deepsense.models.json.graph.GraphJsonProtocol.GraphReader
import ai.deepsense.models.json.workflow.InferredStateJsonProtocol
import ai.deepsense.models.json.workflow.InferredStateJsonProtocol._
import ai.deepsense.models.json.workflow.ExecutionReportJsonProtocol._
import ai.deepsense.models.workflows.{ExecutionReport, InferredState}
import ai.deepsense.workflowexecutor.communication.message.global._
import ai.deepsense.workflowexecutor.communication.message.global.HeartbeatJsonProtocol._
import ai.deepsense.workflowexecutor.communication.message.global.PoisonPillJsonProtocol._
import ai.deepsense.workflowexecutor.communication.message.global.ReadyJsonProtocol._
import ai.deepsense.workflowexecutor.communication.message.global.LaunchJsonProtocol._

object Global {
  val charset = Charset.forName("UTF-8")

  val dOperationsCatalog = CatalogRecorder.resourcesCatalogRecorder.catalogs.operations

  val graphReader = new GraphReader(dOperationsCatalog)

  val inferredStateJsonProtocol = InferredStateJsonProtocol(graphReader)
  import inferredStateJsonProtocol._

  import Constants.MessagesTypes._

  object HeartbeatDeserializer extends DefaultJsonMessageDeserializer[Heartbeat](heartbeat)
  object HeartbeatSerializer extends DefaultJsonMessageSerializer[Heartbeat](heartbeat)

  object PoisonPillDeserializer extends DefaultJsonMessageDeserializer[PoisonPill](poisonPill)
  object PoisonPillSerializer extends DefaultJsonMessageSerializer[PoisonPill](poisonPill)

  object ReadyDeserializer extends DefaultJsonMessageDeserializer[Ready](ready)
  object ReadySerializer extends DefaultJsonMessageSerializer[Ready](ready)

  object LaunchDeserializer extends DefaultJsonMessageDeserializer[Launch](launch)
  object LaunchSerializer extends DefaultJsonMessageSerializer[Launch](launch)

  object ExecutionReportSerializer extends DefaultJsonMessageSerializer[ExecutionReport](executionReport)
  object ExecutionReportDeserializer extends DefaultJsonMessageDeserializer[ExecutionReport](executionReport)

  object InferredStateSerializer extends DefaultJsonMessageSerializer[InferredState](inferredState)
  object InferredStateDeserializer extends DefaultJsonMessageDeserializer[InferredState](inferredState)

  object GlobalMQSerializer extends JsonMQSerializer(
    Seq(HeartbeatSerializer,
      PoisonPillSerializer,
      ReadySerializer,
      LaunchSerializer,
      ExecutionReportSerializer,
      InferredStateSerializer
    ))

  object GlobalMQDeserializer extends JsonMQDeserializer(
    Seq(HeartbeatDeserializer,
      PoisonPillDeserializer,
      ReadyDeserializer,
      LaunchDeserializer,
      ExecutionReportDeserializer,
      InferredStateDeserializer
    ))
}
