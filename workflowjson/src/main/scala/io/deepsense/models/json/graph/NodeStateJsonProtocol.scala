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

import spray.json._

import io.deepsense.commons.exception.json.FailureDescriptionJsonProtocol
import io.deepsense.commons.json.DateTimeJsonProtocol._
import io.deepsense.graph.{Progress, State}

trait NodeStateJsonProtocol
  extends DefaultJsonProtocol
  with FailureDescriptionJsonProtocol
  with NullOptions {
  implicit val progressFormat = jsonFormat2(Progress.apply)

  implicit object NodeStateWriter
    extends JsonWriter[State]
    with DefaultJsonProtocol
    with NullOptions {

    override def write(state: State): JsValue = {
      JsObject(
        NodeStateJsonProtocol.Status -> state.status.toString.toJson,
        NodeStateJsonProtocol.Started -> state.started.toJson,
        NodeStateJsonProtocol.Ended -> state.ended.toJson,
        NodeStateJsonProtocol.Progress -> state.progress.toJson,
        NodeStateJsonProtocol.Results -> state.results.map(_.map(_.toString)).toJson,
        NodeStateJsonProtocol.Error -> state.error.toJson
      )
    }
  }
}

object NodeStateJsonProtocol extends NodeStateJsonProtocol {
  val Status = "status"
  val Started = "started"
  val Ended = "ended"
  val Progress = "progress"
  val Results = "results"
  val Error = "error"
}
