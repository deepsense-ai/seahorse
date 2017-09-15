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

package io.deepsense.graphjson.graph

import spray.json._

import io.deepsense.deeplang.{DKnowledge, DOperable}
import io.deepsense.model.json.graph.GraphKnowledgeJsonProtocol

class GraphKnowledgeJsonProtocolSpec extends GraphJsonTestSupport {

  import io.deepsense.model.json.graph.GraphKnowledgeJsonProtocol._

  val mockOperable1 = mock[DOperable]
  val mockOperable2 = mock[DOperable]
  val mockOperable3 = mock[DOperable]

  val setOfKnowledge = Set(mockOperable3, mockOperable2)
  val knowledgeVector = Vector(
    new DKnowledge(Set(mockOperable1)),
    new DKnowledge(setOfKnowledge))

  "DKnowledgeVector" should {
    "be transformable to Json" in {
      val knowledgeJson = knowledgeVector.toJson.asInstanceOf[JsArray]
      val dKnowledge1 = knowledgeJson.elements.head.convertTo[List[String]]
      dKnowledge1 shouldBe List(mockOperable1.getClass.getName)
      val dKnowledge2 = knowledgeJson.elements(1).convertTo[Set[String]]
      dKnowledge2 shouldBe setOfKnowledge.map(_.getClass.getName)
    }
  }
}
