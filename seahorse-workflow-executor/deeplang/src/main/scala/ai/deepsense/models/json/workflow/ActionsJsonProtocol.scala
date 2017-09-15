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

import spray.httpx.SprayJsonSupport
import spray.json._

import ai.deepsense.models.actions.{AbortAction, Action, LaunchAction}
import ai.deepsense.models.json.graph.NodeJsonProtocol

trait ActionsJsonProtocol
  extends DefaultJsonProtocol
  with SprayJsonSupport
  with NodeJsonProtocol {

  implicit val launchActionFormat = jsonFormat1(LaunchAction.apply)
  implicit val abortActionFormat = jsonFormat1(AbortAction.apply)

  implicit object ActionJsonFormat extends RootJsonReader[Action] {
    val abortName = "abort"
    val launchName = "launch"
    override def read(json: JsValue): Action = json match {
      case JsObject(x) if x.contains(abortName) =>
        x.get(abortName).get.convertTo[AbortAction]
      case JsObject(x) if x.contains(launchName) =>
        x.get(launchName).get.convertTo[LaunchAction]
      case x => deserializationError(s"Expected Abort Action or Launch Action, but got $x")
    }
  }
}

object ActionsJsonProtocol extends ActionsJsonProtocol
