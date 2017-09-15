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

package io.deepsense.models.json.graph

import org.joda.time.DateTime
import spray.json._

import io.deepsense.commons.exception.FailureDescription
import io.deepsense.commons.exception.json.FailureDescriptionJsonProtocol
import io.deepsense.commons.json.DateTimeJsonProtocol._
import io.deepsense.graph.nodestate._
import io.deepsense.models.entities.Entity

trait NodeStateJsonProtocol
  extends DefaultJsonProtocol
  with FailureDescriptionJsonProtocol
  with NullOptions {

  implicit object NodeStateFormat
    extends JsonFormat[NodeState]
    with DefaultJsonProtocol
    with NullOptions {

    val viewFormat = jsonFormat5(NodeStateView)
    val runningFormat = jsonFormat1(Running)
    val completedFormat = jsonFormat3(Completed)
    val failedFormat = jsonFormat3(Failed)

    override def write(state: NodeState): JsValue = {

      val view = state match {

        case Running(started) => NodeStateView(state.name, started = Some(started))
        case Completed(started, ended, results) =>
          NodeStateView(state.name,
            started = Some(started),
            ended = Some(ended),
            results = Some(results))
        case Failed(started, ended, error) =>
          NodeStateView(state.name,
            started = Some(started),
            ended = Some(ended),
            error = Some(error)
          )
        case _ => NodeStateView(state.name)
      }

      view.toJson(viewFormat)
    }

    override def read(json: JsValue): NodeState = {
      val jsObject = json.asJsObject
      val status = jsObject.fields.get("status")
      if (status.isEmpty) {
        throw new DeserializationException("Expected 'status' field does not exist")
      }

      status.get.convertTo[String] match {
        case "DRAFT" => Draft
        case "QUEUED" => Queued
        case "RUNNING" => jsObject.convertTo[Running](runningFormat)
        case "COMPLETED" => jsObject.convertTo[Completed](completedFormat)
        case "FAILED" => jsObject.convertTo[Failed](failedFormat)
        case "ABORTED" => Aborted
      }
    }

    case class NodeStateView(
      status: String,
      started: Option[DateTime] = None,
      ended: Option[DateTime] = None,
      results: Option[Seq[Entity.Id]] = None,
      error: Option[FailureDescription] = None)
  }
}

object NodeStateJsonProtocol extends NodeStateJsonProtocol {
  val Status = "status"
  val Started = "started"
  val Ended = "ended"
  val Results = "results"
  val Error = "error"
}
