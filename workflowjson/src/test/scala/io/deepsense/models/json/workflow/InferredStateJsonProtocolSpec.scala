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

package io.deepsense.models.json.workflow

import org.joda.time.DateTime
import org.mockito.Mockito._
import spray.json._

import io.deepsense.deeplang.exceptions.DeepLangException
import io.deepsense.deeplang.inference.{InferenceWarning, InferenceWarnings}
import io.deepsense.deeplang.params.Params
import io.deepsense.deeplang.{DKnowledge, DOperable}
import io.deepsense.graph.{GraphKnowledge, NodeInferenceResult}
import io.deepsense.models.entities.Entity
import io.deepsense.models.workflows._

class InferredStateJsonProtocolSpec
    extends WorkflowJsonTestSupport
    with InferredStateJsonProtocol {

  "InferredState" should {
    "be serializable to json" in {
      val (inferredState, json) = inferredStateFixture
      inferredState.toJson shouldBe json
    }
  }

  def inferredStateFixture: (InferredState, JsObject) = {
    val workflowId = Workflow.Id.randomId
    val (graphKnowledge, graphKnowledgeJson) = graphKnowledgeFixture

    val (executionStates, statesJson) = executionStatesFixture

    val workflow = InferredState(workflowId, graphKnowledge, executionStates)

    val workflowJson = JsObject(
      "id" -> JsString(workflowId.toString),
      "knowledge" -> graphKnowledgeJson,
      "states" -> statesJson
    )
    (workflow, workflowJson)
  }

  def graphKnowledgeFixture: (GraphKnowledge, JsObject) = {
    val parametricOperable = mock[ParametricOperable]
    val paramSchema: JsString = JsString("Js with ParamSchema")
    val paramValues: JsString = JsString("Js with ParamValues")
    when(parametricOperable.paramsToJson).thenReturn(paramSchema)
    when(parametricOperable.paramValuesToJson).thenReturn(paramValues)

    val graphKnowledge = GraphKnowledge().addInference(
      node1.id,
      NodeInferenceResult(
        Vector(
          DKnowledge(Set(operable)),
          DKnowledge(Set(operable, parametricOperable)),
          DKnowledge(Set[DOperable](parametricOperable))
        ),
        InferenceWarnings(
          new InferenceWarning("warning1") {},
          new InferenceWarning("warning2") {}),
        Vector(
          new DeepLangException("error1") {},
          new DeepLangException("error2") {}
        )
      )
    )

    val knowledgeJson = JsObject(
      node1.id.toString -> JsObject(
        "ports" -> JsArray(
          JsObject(
            "types" -> JsArray(JsString(operable.getClass.getCanonicalName)),
            "params" -> JsNull
          ),
          JsObject(
            "types" -> JsArray(
              JsString(operable.getClass.getCanonicalName),
              JsString(parametricOperable.getClass.getCanonicalName)),
            "params" -> JsNull
          ),
          JsObject(
            "types" -> JsArray(JsString(parametricOperable.getClass.getCanonicalName)),
            "params" -> JsObject(
              "schema" -> paramSchema,
              "values" -> paramValues
            )
          )
        ),
        "warnings" -> JsArray(
          JsString("warning1"),
          JsString("warning2")
        ),
        "errors" -> JsArray(
          JsString("error1"),
          JsString("error2")
        )
      )
    )

    (graphKnowledge, knowledgeJson)
  }


  def executionStatesFixture: (ExecutionReport, JsObject) = {

    val startTimestamp = "2015-05-12T21:11:09.000Z"
    val finishTimestamp = "2015-05-12T21:12:50.000Z"

    val entity1Id = Entity.Id.randomId
    val entity2Id = Entity.Id.randomId

    val executionStates = ExecutionReport.statesOnly(
      Map(
        node1.id -> io.deepsense.graph.nodestate.Completed(
          DateTime.parse(startTimestamp),
          DateTime.parse(finishTimestamp),
          Seq(entity1Id, entity2Id)
        )
      ),
      None)
    val executionStatesJson = JsObject(
      "error" -> JsNull,
      "nodes" -> JsObject(
        node1.id.toString -> JsObject(
          "status" -> JsString("COMPLETED"),
          "started" -> JsString(startTimestamp),
          "ended" -> JsString(finishTimestamp),
          "results" -> JsArray(
            JsString(entity1Id.toString),
            JsString(entity2Id.toString)
          ),
          "error" -> JsNull
        )
      ),
      "resultEntities" -> JsObject()
    )

    (executionStates, executionStatesJson)
  }

  abstract class ParametricOperable extends DOperable with Params
}
