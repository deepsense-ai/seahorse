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

import org.apache.spark.sql.types.StructType
import spray.json._

import io.deepsense.deeplang._
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.doperations.custom.{Sink, Source}
import io.deepsense.deeplang.inference.InferContext
import io.deepsense.deeplang.params.{WorkflowParam, Param}
import io.deepsense.graph.{GraphKnowledge, Node, NodeInferenceResult}

case class CustomTransformer() extends Transformer with DefaultCustomTransformerWorkflow {

  val innerWorkflow = WorkflowParam(
    name = "inner workflow",
    description = "Inner workflow of the Transformer.")
  setDefault(innerWorkflow, defaultWorkflow)

  def getInnerWorkflow: JsObject = $(innerWorkflow)
  def setInnerWorkflow(workflow: JsObject): this.type = set(innerWorkflow, workflow)

  override val params: Array[Param[_]] = declareParams(innerWorkflow)

  override private[deeplang] def _transform(ctx: ExecutionContext, df: DataFrame): DataFrame = {
    val workflow = ctx.innerWorkflowExecutor.parse(getInnerWorkflow)
    ctx.innerWorkflowExecutor.execute(CommonExecutionContext(ctx), workflow, df)
  }

  override private[deeplang] def _transformSchema(
      schema: StructType, inferCtx: InferContext): Option[StructType] = {
    val workflow = inferCtx.innerWorkflowParser.parse(getInnerWorkflow)
    val initialKnowledge = GraphKnowledge(Map(
      workflow.source -> NodeInferenceResult(Vector(DKnowledge(DataFrame.forInference(schema))))
    ))

    workflow.graph.inferKnowledge(inferCtx, initialKnowledge)
      .getKnowledge(workflow.sink)(0).asInstanceOf[DKnowledge[DataFrame]].single.schema
  }

  override def report(executionContext: ExecutionContext): Report = Report()
}

trait DefaultCustomTransformerWorkflow {

  private def node(operation: DOperation, nodeId: Node.Id): JsObject =
    JsObject(
      "id" -> JsString(nodeId.toString),
      "operation" -> JsObject(
        "id" -> JsString(operation.id.toString),
        "name" -> JsString(operation.name)
      ),
      "parameters" -> JsObject()
    )

  private def connection(from: Node.Id, to: Node.Id): JsObject =
    JsObject(
      "from" -> JsObject(
        "nodeId" -> JsString(from.toString),
        "portIndex" -> JsNumber(0)
      ),
      "to" -> JsObject(
        "nodeId" -> JsString(to.toString),
        "portIndex" -> JsNumber(0)
      )
    )

  private val sourceNodeId: Node.Id = "2603a7b5-aaa9-40ad-9598-23f234ec5c32"
  private val sinkNodeId: Node.Id = "d7798d5e-b1c6-4027-873e-a6d653957418"

  val defaultWorkflow = JsObject(
    "workflow" -> JsObject(
      "nodes" -> JsArray(node(Source(), sourceNodeId), node(Sink(), sinkNodeId)),
      "connections" -> JsArray(connection(sourceNodeId, sinkNodeId)),
      "thirdPartyData" -> JsString("{}"),
      "source" -> JsString(sourceNodeId.toString),
      "sink" -> JsString(sinkNodeId.toString)
    )
  )
}
