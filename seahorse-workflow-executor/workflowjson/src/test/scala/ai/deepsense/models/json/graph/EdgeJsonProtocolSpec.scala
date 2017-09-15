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

import ai.deepsense.graph.{Edge, Endpoint, Node}

class EdgeJsonProtocolSpec extends GraphJsonTestSupport {

  import ai.deepsense.models.json.graph.EdgeJsonProtocol._

  val expectedFromId: Node.Id = Node.Id.randomId
  val expectedFromPort = 1989
  val expectedToId: Node.Id = Node.Id.randomId
  val expectedToPort = 1337
  val edge = Edge(
    Endpoint(expectedFromId, expectedFromPort),
    Endpoint(expectedToId, expectedToPort)
  )

  "Edge transformed to Json" should {
    "have correct from and to" in {
      val edgeJson = edge.toJson.asJsObject
      assertEndpointMatchesJsObject(edge.from, edgeJson.fields("from").asJsObject)
      assertEndpointMatchesJsObject(edge.to, edgeJson.fields("to").asJsObject)
    }
  }

  "Edge transformed to Json and then read to Object" should {
    "be equal" in {
      edge.toJson.convertTo[Edge] shouldBe edge
    }
  }
}
