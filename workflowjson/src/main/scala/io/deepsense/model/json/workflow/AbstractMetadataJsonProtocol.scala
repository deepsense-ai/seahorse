/**
 * Copyright 2015, CodiLime Inc.
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

package io.deepsense.model.json.workflow

import spray.httpx.SprayJsonSupport
import spray.json._

import io.deepsense.deeplang.DOperable.AbstractMetadata

trait AbstractMetadataJsonProtocol extends DefaultJsonProtocol with SprayJsonSupport {
  implicit object AbstractMetadataFormat extends RootJsonFormat[AbstractMetadata] {
    override def write(am: AbstractMetadata): JsValue = am.serializeToJson
    override def read(value: JsValue): AbstractMetadata = ???
  }
}

object AbstractMetadataJsonProtocol extends AbstractMetadataJsonProtocol
