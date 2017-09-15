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

import org.scalatest.BeforeAndAfter
import org.scalatest.mockito.MockitoSugar
import spray.json._

import ai.deepsense.commons.StandardSpec

class WorkflowJsonParamsOverriderSpec
    extends StandardSpec
    with BeforeAndAfter
    with MockitoSugar
    with DefaultJsonProtocol {

  "WorkflowJsonParamsOverrider" should {
    "override parameters based on passed extra params" in {
      val overrides = Map(
        "node1.param with spaces" -> "new value",
        "node2.nested.parameter.test" -> "changed"
      )

      WorkflowJsonParamsOverrider.overrideParams(originalJson, overrides) shouldBe expectedJson
    }

    "throw when invalid parameters are passed" in {
      val overrides = Map(
        "node1.no such param" -> "no such param",
        "no such node.param" -> "no such node"
      )

      a[RuntimeException] should be thrownBy {
        WorkflowJsonParamsOverrider.overrideParams(originalJson, overrides)
      }
    }
  }

  val originalJson =
    """{
      |  "workflow": {
      |    "nodes": [{
      |      "id": "node1",
      |      "parameters": {
      |        "param with spaces": "value"
      |      }
      |     }, {
      |      "id": "node2",
      |      "parameters": {
      |        "param": "value",
      |        "nested": {
      |          "parameter": {
      |            "test": "nested value"
      |          }
      |        }
      |      }
      |    }]
      |  }
      |}
    """.stripMargin.parseJson

  val expectedJson =
    """{
      |  "workflow": {
      |    "nodes": [{
      |      "id": "node1",
      |      "parameters": {
      |        "param with spaces": "new value"
      |      }
      |     }, {
      |      "id": "node2",
      |      "parameters": {
      |        "param": "value",
      |        "nested": {
      |          "parameter": {
      |            "test": "changed"
      |          }
      |        }
      |      }
      |    }]
      |  }
      |}
    """.stripMargin.parseJson
}
