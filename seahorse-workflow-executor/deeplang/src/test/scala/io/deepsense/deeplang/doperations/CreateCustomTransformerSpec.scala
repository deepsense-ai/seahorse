/**
 * Copyright 2016, deepsense.io
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

package io.deepsense.deeplang.doperations

import spray.json.JsObject

import io.deepsense.deeplang._
import io.deepsense.deeplang.doperables.{CustomTransformer, TargetTypeChoices, TypeConverter}
import io.deepsense.deeplang.doperations.custom.{Sink, Source}
import io.deepsense.deeplang.inference.InferContext
import io.deepsense.deeplang.params.ParameterType
import io.deepsense.deeplang.params.custom.{InnerWorkflow, PublicParam}
import io.deepsense.deeplang.params.selections.{MultipleColumnSelection, NameColumnSelection}
import io.deepsense.graph.{DeeplangGraph, Edge, Node}
import io.deepsense.models.json.graph.GraphJsonProtocol.GraphReader

class CreateCustomTransformerSpec extends UnitSpec {

  val node1Id = Node.Id.randomId
  val node2Id = Node.Id.randomId

  object MockCreateCustomTransformer extends CreateCustomTransformer {
    def createWithParam: CreateCustomTransformer = {
      set(innerWorkflow, createInnerWorkflow(
        PublicParam(node1Id, "target type", "public param 1"),
        PublicParam(node2Id, "target type", "public param 2")
      ))
    }
  }

  "CreateCustomTransformer" should {
    "create CustomTransformer with public params" in {

      val operation = MockCreateCustomTransformer.createWithParam
      val executionContext = mock[ExecutionContext]

      val results = operation.executeUntyped(Vector.empty)(executionContext)
      results.length shouldBe 1
      results(0) shouldBe a[CustomTransformer]
      val result = results(0).asInstanceOf[CustomTransformer]

      result.params.length shouldBe 2

      result.params(0).name shouldBe "public param 1"
      result.params(0).parameterType shouldBe ParameterType.Choice

      result.params(1).name shouldBe "public param 2"
      result.params(1).parameterType shouldBe ParameterType.Choice
    }

    "create CustomTransformer without public params" in {
      val operation = CreateCustomTransformer()
      val executionContext = mock[ExecutionContext]

      val results = operation.executeUntyped(Vector.empty)(executionContext)
      results.size shouldBe 1
      results(0) shouldBe a[CustomTransformer]
      val result = results(0).asInstanceOf[CustomTransformer]

      result.params.length shouldBe 0
    }

    "infer parameters of CustomTransformer from input inner workflow" in {
      val operation = MockCreateCustomTransformer.createWithParam
      val inferContext = mock[InferContext]

      val results = operation.inferKnowledgeUntyped(Vector.empty)(inferContext)._1.map(_.single)
      results.length shouldBe 1
      results(0) shouldBe a[CustomTransformer]
      val result = results(0).asInstanceOf[CustomTransformer]

      result.params.length shouldBe 2

      result.params(0).name shouldBe "public param 1"
      result.params(0).parameterType shouldBe ParameterType.Choice

      result.params(1).name shouldBe "public param 2"
      result.params(1).parameterType shouldBe ParameterType.Choice
    }
  }

  private def createInnerWorkflow(publicParams: PublicParam*): InnerWorkflow = {
    val graphReader = mock[GraphReader]
    val sourceNodeId = "2603a7b5-aaa9-40ad-9598-23f234ec5c32"
    val sinkNodeId = "d7798d5e-b1c6-4027-873e-a6d653957418"

    val sourceNode = Node(sourceNodeId, Source())
    val sinkNode = Node(sinkNodeId, Sink())

    val node1Operation = {
      val params = TypeConverter()
        .setTargetType(TargetTypeChoices.StringTargetTypeChoice())
        .setSelectedColumns(MultipleColumnSelection(Vector(NameColumnSelection(Set("column1")))))
        .paramValuesToJson
      new ConvertType().setParamsFromJson(params, graphReader)
    }

    val node2Operation = {
      val params = TypeConverter()
        .setTargetType(TargetTypeChoices.StringTargetTypeChoice())
        .setSelectedColumns(MultipleColumnSelection(Vector(NameColumnSelection(Set("column1")))))
        .paramValuesToJson
      new ConvertType().setParamsFromJson(params, graphReader)
    }

    val node1 = Node(node1Id, node1Operation)
    val node2 = Node(node2Id, node2Operation)

    val simpleGraph = DeeplangGraph(
      Set(sourceNode, sinkNode, node1, node2),
      Set(Edge(sourceNode, 0, node1, 0), Edge(node1, 0, node2, 0), Edge(node2, 0, sinkNode, 0)))

    InnerWorkflow(simpleGraph, JsObject(), publicParams.toList)
  }
}
