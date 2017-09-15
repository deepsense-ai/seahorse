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

package io.deepsense.deeplang.doperables

import org.apache.spark.SparkContext
import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.types._
import org.mockito.Matchers._
import org.mockito.Mockito.when
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import spray.json.JsObject

import io.deepsense.deeplang._
import io.deepsense.deeplang.catalogs.doperable.DOperableCatalog
import io.deepsense.deeplang.doperables.dataframe.{DataFrame, DataFrameBuilder}
import io.deepsense.deeplang.doperations.ConvertType
import io.deepsense.deeplang.doperations.custom.{Sink, Source}
import io.deepsense.deeplang.inference.InferContext
import io.deepsense.deeplang.params.custom.{PublicParam, InnerWorkflow}
import io.deepsense.deeplang.params.selections.{MultipleColumnSelection, NameColumnSelection}
import io.deepsense.graph.{DeeplangGraph, Edge, Node}

class CustomTransformerSpec extends UnitSpec {

  val sourceNodeId = "2603a7b5-aaa9-40ad-9598-23f234ec5c32"
  val sinkNodeId = "d7798d5e-b1c6-4027-873e-a6d653957418"
  val innerNodeId = "b22bd79e-337d-4223-b9ee-84c2526a1b75"

  val sourceNode = Node(sourceNodeId, Source())
  val sinkNode = Node(sinkNodeId, Sink())

  private def createInnerNodeOperation(targetType: TargetTypeChoice) = {
    val params = TypeConverter()
      .setTargetType(targetType)
      .setSelectedColumns(MultipleColumnSelection(Vector(NameColumnSelection(Set("column1")))))
      .paramValuesToJson
    new ConvertType().setParamsFromJson(params)
  }

  private def createInnerNode(targetType: TargetTypeChoice) =
    Node(innerNodeId, createInnerNodeOperation(targetType))

  def simpleGraph(
      targetType: TargetTypeChoice = TargetTypeChoices.StringTargetTypeChoice()): DeeplangGraph = {
    val innerNode = createInnerNode(targetType)
    DeeplangGraph(
      Set(sourceNode, sinkNode, innerNode),
      Set(Edge(sourceNode, 0, innerNode, 0), Edge(innerNode, 0, sinkNode, 0)))
  }

  "CustomTransfromer" should {

    "execute inner workflow" in {
      val workflow = InnerWorkflow(simpleGraph(), JsObject())
      val outputDataFrame = mock[DataFrame]

      val innerWorkflowExecutor = mock[InnerWorkflowExecutor]
      when(innerWorkflowExecutor.execute(any(), same(workflow), any())).thenReturn(outputDataFrame)

      val context = mockExecutionContext(innerWorkflowExecutor)

      val transformer = new CustomTransformer(workflow, Array.empty)

      transformer._transform(context, mock[DataFrame]) shouldBe outputDataFrame
    }

    "infer knowledge" in {
      val innerWorkflowParser = mock[InnerWorkflowParser]
      val catalog = new DOperableCatalog()
      CatalogRecorder.registerDOperables(catalog)
      val inferContext = InferContext(
        mock[DataFrameBuilder],
        "",
        catalog,
        innerWorkflowParser)

      val transformer = new CustomTransformer(InnerWorkflow(simpleGraph(), JsObject()), Array.empty)

      val schema = StructType(Seq(
        StructField("column1", DoubleType),
        StructField("column2", DoubleType)
      ))

      val expectedSchema = StructType(Seq(
        StructField("column1", StringType),
        StructField("column2", DoubleType)
      ))

      transformer._transformSchema(schema, inferContext) shouldBe Some(expectedSchema)
    }

    "set public params" when {
      "executing inner workflow" in {
        val innerParam = TypeConverter().targetType
        val publicParam = TypeConverter().targetType.replicate("public name")
        val customTargetType = TargetTypeChoices.LongTargetTypeChoice()

        val workflow = InnerWorkflow(simpleGraph(), JsObject(),
          List(PublicParam(innerNodeId, "target type", "public name")))

        val outputDataFrame = mock[DataFrame]

        val innerWorkflowExecutor = mock[InnerWorkflowExecutor]
        when(innerWorkflowExecutor.execute(any(), any(), any()))
          .thenAnswer(new Answer[DataFrame] {
            override def answer(invocation: InvocationOnMock): DataFrame = {
              val workflow = invocation.getArguments.apply(1).asInstanceOf[InnerWorkflow]
              val innerOp = workflow.graph.nodes.find(_.id.toString == innerNodeId).get.value
                .asInstanceOf[ConvertType]

              innerOp.get(innerParam) shouldBe Some(customTargetType)

              outputDataFrame
            }
          })

        val context = mockExecutionContext(innerWorkflowExecutor)

        val transformer = new CustomTransformer(workflow, Array(publicParam))
        transformer.set(publicParam -> customTargetType)

        transformer._transform(context, mock[DataFrame]) shouldBe outputDataFrame
      }

      "inferring schema" in {
        val innerWorkflowParser = mock[InnerWorkflowParser]
        val catalog = new DOperableCatalog()
        CatalogRecorder.registerDOperables(catalog)
        val inferContext = InferContext(
          mock[DataFrameBuilder],
          "",
          catalog,
          innerWorkflowParser)

        val publicParam = TypeConverter().targetType.replicate("public name")

        val transformer = new CustomTransformer(InnerWorkflow(simpleGraph(), JsObject(),
          List(PublicParam(innerNodeId, "target type", "public name"))), Array(publicParam))

        transformer.set(publicParam -> TargetTypeChoices.LongTargetTypeChoice())

        val schema = StructType(Seq(
          StructField("column1", DoubleType),
          StructField("column2", DoubleType)
        ))

        val expectedSchema = StructType(Seq(
          StructField("column1", LongType),
          StructField("column2", DoubleType)
        ))

        transformer._transformSchema(schema, inferContext) shouldBe Some(expectedSchema)
      }
    }
  }

  private def mockExecutionContext(
      innerWorkflowExecutor: InnerWorkflowExecutor): ExecutionContext =
    ExecutionContext(
      mock[SparkContext],
      mock[SQLContext],
      mock[InferContext],
      mock[FileSystemClient],
      "",
      innerWorkflowExecutor,
      mock[ContextualDataFrameStorage],
      mock[ContextualPythonCodeExecutor]
    )
}
