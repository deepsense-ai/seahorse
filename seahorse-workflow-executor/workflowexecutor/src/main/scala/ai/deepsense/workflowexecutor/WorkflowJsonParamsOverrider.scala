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

package ai.deepsense.workflowexecutor

import spray.json.lenses.JsonLenses._
import spray.json.{DefaultJsonProtocol, JsValue}

object WorkflowJsonParamsOverrider extends DefaultJsonProtocol {

  def overrideParams(workflow: JsValue, params: Map[String, String]): JsValue = {
    params.foldLeft(workflow)(overrideParam)
  }

  private def overrideParam(json: JsValue, param: (String, String)): JsValue = {
    val (key, value) = param
    val pathElems = key.split("\\.")
    val basePath =
      "workflow" / "nodes" / filter("id".is[String](_ == pathElems(0))) / "parameters"
    val path = pathElems.drop(1).foldLeft(basePath)((a, b) => a / b)
    json.update(path ! modify[String](_ => value))
  }
}
