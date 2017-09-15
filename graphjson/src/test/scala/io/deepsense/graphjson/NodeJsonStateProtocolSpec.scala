/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.graphjson

import org.joda.time.DateTime
import org.mockito.Mockito._
import spray.json._

import io.deepsense.commons.datetime.DateTimeConverter
import io.deepsense.commons.exception.{DeepSenseFailure, FailureDescription}
import io.deepsense.commons.json.DateTimeJsonProtocol
import io.deepsense.graph.{Progress => GraphProgress, State}
import io.deepsense.model.json.graph.NodeStateJsonProtocol
import io.deepsense.models.entities.Entity
import io.deepsense.commons.exception.FailureCode

class NodeJsonStateProtocolSpec extends GraphJsonTestSupport {

  import DateTimeJsonProtocol._
  import NodeStateJsonProtocol._

  val status = io.deepsense.graph.Status.Completed

  "Node state translated to Json" should {
    "have all fields printed if they were set" in {
      val started = DateTimeConverter.now
      val ended = DateTimeConverter.now
      val progress = GraphProgress(3, 14)
      val error = FailureDescription(
        DeepSenseFailure.Id.randomId,
        FailureCode.UnexpectedError,
        "something wrong")
      val results = List(Entity.Id.randomId, Entity.Id.randomId, Entity.Id.randomId)
      val state = mock[State]
      when(state.status).thenReturn(status)
      when(state.started).thenReturn(Some(started))
      when(state.ended).thenReturn(Some(ended))
      when(state.progress).thenReturn(Some(progress))
      when(state.error).thenReturn(Some(error))
      when(state.results).thenReturn(Some(results))
      val stateJson = state.toJson(NodeStateJsonProtocol.NodeStateWriter).asJsObject
      stateJson.fields.keys.size shouldBe 6
      stateJson.fields("started").convertTo[DateTime] shouldBe started
      stateJson.fields("ended").convertTo[DateTime] shouldBe ended
      stateJson.fields("progress").convertTo[GraphProgress] shouldBe progress
      stateJson.fields("results").convertTo[List[String]] shouldBe results.map(_.toString)
      stateJson.fields("error").convertTo[FailureDescription] shouldBe error
    }
    "have optional fields printed as null if they were not set" in {
      val state = mock[State]
      when(state.status).thenReturn(status)
      when(state.started).thenReturn(None)
      when(state.ended).thenReturn(None)
      when(state.progress).thenReturn(None)
      when(state.results).thenReturn(None)
      when(state.error).thenReturn(None)
      val stateJson = state.toJson(NodeStateJsonProtocol.NodeStateWriter).asJsObject
      stateJson.fields.keys.size shouldBe 6
      stateJson.fields.keys should contain("status")
      stateJson.fields("status").convertTo[String] shouldBe status.toString.toUpperCase
      stateJson.fields("started") shouldBe JsNull
      stateJson.fields("ended") shouldBe JsNull
      stateJson.fields("progress") shouldBe JsNull
      stateJson.fields("results") shouldBe JsNull
      stateJson.fields("error") shouldBe JsNull
    }
  }
}
