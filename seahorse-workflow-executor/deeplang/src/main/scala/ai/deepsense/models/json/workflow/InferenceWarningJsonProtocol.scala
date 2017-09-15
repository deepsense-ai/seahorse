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

import ai.deepsense.deeplang.inference.{InferenceWarning, InferenceWarnings}

trait InferenceWarningJsonProtocol extends DefaultJsonProtocol with SprayJsonSupport {
  implicit object InferenceWarningMappingFormat extends JsonFormat[InferenceWarning] {
    override def write(warning: InferenceWarning): JsValue = JsString(warning.message)
    override def read(value: JsValue): InferenceWarning =
      new InferenceWarning(value.asInstanceOf[JsString].value) {}
  }
}

trait InferenceWarningsJsonProtocol extends DefaultJsonProtocol
    with SprayJsonSupport
    with InferenceWarningJsonProtocol {

  implicit object InferenceWarningsMappingFormat extends JsonFormat[InferenceWarnings] {
    override def write(warnings: InferenceWarnings): JsValue = warnings.warnings.toJson
    override def read(value: JsValue): InferenceWarnings =
      InferenceWarnings(value.asInstanceOf[JsArray].convertTo[Vector[InferenceWarning]])
  }
}
