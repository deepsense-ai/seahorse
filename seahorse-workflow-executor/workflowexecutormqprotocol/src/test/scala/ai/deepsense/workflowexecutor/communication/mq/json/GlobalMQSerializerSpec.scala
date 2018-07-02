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

import java.nio.charset.StandardCharsets

import org.scalatest.mockito.MockitoSugar
import spray.json._

import ai.deepsense.commons.StandardSpec
import ai.deepsense.commons.models.Entity
import ai.deepsense.deeplang.DOperable
import ai.deepsense.deeplang.doperables.ColumnsFilterer
import ai.deepsense.graph.Node
import ai.deepsense.models.json.workflow.ExecutionReportJsonProtocol._
import ai.deepsense.models.workflows.{EntitiesMap, ExecutionReport, Workflow}
import ai.deepsense.reportlib.model.factory.ReportContentTestFactory
import ai.deepsense.workflowexecutor.communication.message.global._
import ai.deepsense.workflowexecutor.communication.mq.json.Global.GlobalMQSerializer

class GlobalMQSerializerSpec
  extends StandardSpec
  with MockitoSugar {

    "GlobalMQSerializer" should {
      "serialize ExecutionReport" in {
        val executionReport = ExecutionReport(
          Map(Node.Id.randomId -> ai.deepsense.graph.nodestate.Draft()),
          EntitiesMap(
            Map[Entity.Id, DOperable](
              Entity.Id.randomId -> new ColumnsFilterer),
            Map(Entity.Id.randomId -> ReportContentTestFactory.someReport)),
          None)

        serialize(executionReport) shouldBe asBytes(JsObject(
          "messageType" -> JsString("executionStatus"),
          "messageBody" -> executionReport.toJson))
      }

      "serialize Launch messages" in {
        val workflowId = Workflow.Id.randomId
        val nodesToExecute = Vector(Workflow.Id.randomId, Workflow.Id.randomId, Workflow.Id.randomId)
        val jsNodesToExecute = JsArray(nodesToExecute.map(id => JsString(id.toString)))

        val outMessage = JsObject(
          "messageType" -> JsString("launch"),
          "messageBody" -> JsObject(
            "workflowId" -> JsString(workflowId.toString),
            "nodesToExecute" -> jsNodesToExecute
          )
        )

        val serializedMessage = serialize(Launch(workflowId, nodesToExecute.toSet))
        serializedMessage shouldBe asBytes(outMessage)
      }

      "serialize Heartbeat without SparkUi messages" in {
        val workflowId = "foo-workflow"
        val outMessage = JsObject(
          "messageType" -> JsString("heartbeat"),
          "messageBody" -> JsObject(
            "workflowId" -> JsString(workflowId)))
        serialize(Heartbeat(workflowId, None)) shouldBe asBytes(outMessage)
      }
      "serialize Heartbeat with SparkUi messages" in {
        val workflowId = "foo-workflow"
        val outMessage = JsObject(
          "messageType" -> JsString("heartbeat"),
          "messageBody" -> JsObject(
            "workflowId" -> JsString(workflowId),
            "sparkUiAddress" -> JsString("localhost")))
        serialize(Heartbeat(workflowId, Some("localhost"))) shouldBe asBytes(outMessage)
      }
      "serialize PoisonPill messages" in {
        val outMessage = JsObject(
          "messageType" -> JsString("poisonPill"),
          "messageBody" -> JsObject())
        serialize(PoisonPill()) shouldBe asBytes(outMessage)
      }
      "serialize Ready messages" in {
        val sessionId = "foo-session"
        val outMessage = JsObject(
          "messageType" -> JsString("ready"),
          "messageBody" -> JsObject(
            "sessionId" -> JsString(sessionId)))
        serialize(Ready(sessionId)) shouldBe asBytes(outMessage)
      }
    }

    private def asBytes(jsObject: JsObject): Array[Byte] =
      jsObject.compactPrint.getBytes(StandardCharsets.UTF_8)

    private def serialize(message: Any): Array[Byte] =
      GlobalMQSerializer.serializeMessage(message)
}
