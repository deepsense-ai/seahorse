/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Wojciech Jurczyk
 */

package io.deepsense.graphjson

import spray.json._

import io.deepsense.deeplang.{DKnowledge, DOperable}

class GraphKnowledgeJsonProtocolSpec extends GraphJsonTestSupport {

  import GraphKnowledgeJsonProtocol._

  val mockOperable1 = mock[DOperable]
  val mockOperable2 = mock[DOperable]
  val mockOperable3 = mock[DOperable]

  val setOfKnowledge = Set(mockOperable3,mockOperable2)
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
