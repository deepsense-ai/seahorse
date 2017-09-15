/**
 * Copyright 2015, deepsense.io
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

package io.deepsense.workflowexecutor.communication.mq.serialization.json

import java.nio.charset.Charset

import org.scalatest.mock.MockitoSugar
import spray.json._

import io.deepsense.commons.StandardSpec
import io.deepsense.deeplang.DOperable
import io.deepsense.deeplang.doperables.ColumnsFilterer
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.doperations.Fit
import io.deepsense.graph.{DirectedGraph, Node}
import io.deepsense.models.entities.Entity
import io.deepsense.models.json.graph.GraphJsonProtocol.GraphReader
import io.deepsense.models.json.workflow.{WorkflowWithResultsJsonProtocol, ExecutionReportJsonProtocol}
import io.deepsense.models.workflows._
import io.deepsense.reportlib.model.ReportContent
import io.deepsense.workflowexecutor.communication.message.global.{PythonGatewayAddressJsonProtocol, Address, PythonGatewayAddress}
import io.deepsense.workflowexecutor.communication.message.workflow.ExecutionStatus

class ProtocolJsonSerializerSpec
  extends StandardSpec
  with MockitoSugar
  with ExecutionReportJsonProtocol
  with PythonGatewayAddressJsonProtocol
  with WorkflowWithResultsJsonProtocol {

  override val graphReader: GraphReader = mock[GraphReader]

  "ProtocolJsonSerializer" should {
    val protocolJsonSerializer = ProtocolJsonSerializer(graphReader)

    "serialize ExecutionStatus" in {
      val executionStatus = ExecutionStatus(
        Map(Node.Id.randomId -> io.deepsense.graph.nodestate.Draft),
        EntitiesMap(
          Map[Entity.Id, DOperable](
            Entity.Id.randomId -> new ColumnsFilterer),
          Map(Entity.Id.randomId -> ReportContent("whatever", List.empty))),
        None)

      protocolJsonSerializer.serializeMessage(executionStatus) shouldBe
      expectedSerializationResult("executionStatus", executionStatus.executionReport.toJson)
    }

    "serialize PythonGatewayAddress" in {
      val pythonGatewayAddress = PythonGatewayAddress(
        List(Address("south.park", 123), Address("not.funny.com", 1111)))

      protocolJsonSerializer.serializeMessage(pythonGatewayAddress) shouldBe
      expectedSerializationResult("pythonGatewayAddress", pythonGatewayAddress.toJson)
    }

    "serialize WorkflowWithResults" in {
      val workflowWithResults = WorkflowWithResults(
        Workflow.Id.randomId,
        WorkflowMetadata(WorkflowType.Streaming, "1.0.0"),
        DirectedGraph(),
        ThirdPartyData("{}"),
        ExecutionReport(Map()))

      protocolJsonSerializer.serializeMessage(workflowWithResults) shouldBe
      expectedSerializationResult("workflowWithResults", workflowWithResults.toJson)
    }
  }

  private def expectedSerializationResult(messageType: String, jsonObject: JsValue): Array[Byte] = {
    JsObject(
      "messageType" -> JsString(messageType),
      "messageBody" -> jsonObject
    ).compactPrint.getBytes(Charset.forName("UTF-8"))
  }
}
