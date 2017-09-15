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

import ai.deepsense.graph.{Edge, Endpoint}

trait EdgeJsonProtocol extends DefaultJsonProtocol {

  import NodeJsonProtocol._

  implicit val EndpointFormat = jsonFormat2(Endpoint)
  implicit val EdgeFormat = jsonFormat2(Edge.apply)
}

object EdgeJsonProtocol extends EdgeJsonProtocol
