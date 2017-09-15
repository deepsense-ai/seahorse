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

package ai.deepsense.models.json.graph

import spray.json._

import ai.deepsense.commons.datetime.DateTimeConverter
import ai.deepsense.commons.exception.{DeepSenseFailure, FailureCode, FailureDescription}
import ai.deepsense.commons.models.Entity
import ai.deepsense.graph.nodestate._

class NodeStatusJsonProtocolSpec extends GraphJsonTestSupport {

  import ai.deepsense.commons.json.DateTimeJsonProtocol._
  import ai.deepsense.models.json.graph.NodeStatusJsonProtocol._

  "NodeStateJsonProtocol" should {
    "transform Draft to Json" in {
      toJs(Draft(results)) shouldBe draftJson
    }
    "read Draft from Json" in {
      fromJs(draftJson) shouldBe Draft(results)
    }
    "transform Queued to Json" in {
      toJs(Queued(results)).toJson shouldBe queuedJson
    }
    "read Queued from Json" in {
      fromJs(queuedJson) shouldBe Queued(results)
    }
    "transform Running to Json" in {
      toJs(running) shouldBe
        runningJson
    }
    "read Running from Json" in {
      fromJs(runningJson) shouldBe running
    }
    "transform Completed to Json" in {
      toJs(completed) shouldBe completedJson
    }
    "read Completed from Json" in {
      fromJs(completedJson) shouldBe completed
    }
    "transform Failed to Json" in {
      toJs(failed) shouldBe failedJson
    }
    "read Failed from Json" in {
      fromJs(failedJson) shouldBe failed
    }
    "transform Aborted to Json" in {
      toJs(Aborted(results)) shouldBe abortedJson
    }
    "read Aborted from Json" in {
      fromJs(abortedJson) shouldBe Aborted(results)
    }
  }

  def fromJs(queuedJson: JsObject): NodeStatus = {
    queuedJson.convertTo[NodeStatus]
  }

  def toJs(state: NodeStatus): JsValue = state.toJson

  def js(state: String, fields: (String, JsValue)*): JsObject = {
    val emptyMap = Seq(
      NodeStatusJsonProtocol.Status,
      NodeStatusJsonProtocol.Started,
      NodeStatusJsonProtocol.Ended,
      NodeStatusJsonProtocol.Results,
      NodeStatusJsonProtocol.Error).map(key => key -> None).toMap[String, Option[JsValue]]

    val jsFields = (emptyMap ++ fields.toMap.mapValues(Some(_)) +
        (NodeStatusJsonProtocol.Status -> Some(JsString(state)))).mapValues {
      case None => JsNull
      case Some(v) => v
    }
    JsObject(jsFields)
  }

  val started = DateTimeConverter.now
  val ended = started.plusDays(1)
  val error = FailureDescription(
    DeepSenseFailure.Id.randomId,
    FailureCode.CannotUpdateRunningWorkflow,
    "This is a test FailureDescription",
    Some("This is a long test description"),
    Map("detail1" -> "value1", "detail2" -> "value2")
  )
  val results = Seq(Entity.Id.randomId, Entity.Id.randomId, Entity.Id.randomId)

  val failed = Failed(started, ended, error)
  val completed = Completed(started, ended, results)
  val running: Running = Running(started, results)
  val failedJson: JsObject = js("FAILED",
    "started" -> started.toJson,
    "ended" -> ended.toJson,
    "error" -> error.toJson)
  val completedJson: JsObject = js("COMPLETED",
    "started" -> started.toJson,
    "ended" -> ended.toJson,
    "results" -> results.toJson)
  val runningJson: JsObject =
    js("RUNNING", "started" -> started.toJson, "results" -> results.toJson)
  val abortedJson: JsObject = js("ABORTED", "results" -> results.toJson)
  val queuedJson: JsObject = js("QUEUED", "results" -> results.toJson)
  val draftJson: JsObject = js("DRAFT", "results" -> results.toJson)
}
