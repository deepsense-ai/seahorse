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

import io.deepsense.deeplang._
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.inference.InferContext
import io.deepsense.deeplang.params.{Param, StringParam}
import io.deepsense.graph.{GraphKnowledge, NodeInferenceResult}

case class CustomTransformer() extends Transformer with DefaultCustomTransformerWorkflow {

  val innerWorkflow = StringParam(
    name = "inner workflow",
    description = "Inner workflow of the Transformer")
  setDefault(innerWorkflow, defaultWorkflow)

  def getInnerWorkflow: String = $(innerWorkflow)
  def setInnerWorkflow(workflow: String): this.type = set(innerWorkflow, workflow)

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

  val defaultWorkflow =
    """{
      |  "workflow": {
      |    "nodes": [
      |      {
      |        "id": "2603a7b5-aaa9-40ad-9598-23f234ec5c32",
      |        "operation": {
      |          "id": "f94b04d7-ec34-42f7-8100-93fe235c89f8",
      |          "name": "Source"
      |        },
      |        "parameters": {}
      |      }, {
      |        "id": "d7798d5e-b1c6-4027-873e-a6d653957418",
      |        "operation": {
      |          "id": "e652238f-7415-4da6-95c6-ee33808561b2",
      |          "name": "Sink"
      |        },
      |        "parameters": {}
      |      }
      |    ],
      |    "connections": [
      |      {
      |        "from":{
      |          "nodeId": "2603a7b5-aaa9-40ad-9598-23f234ec5c32",
      |          "portIndex": 0
      |        },
      |        "to": {
      |          "nodeId": "d7798d5e-b1c6-4027-873e-a6d653957418",
      |          "portIndex":0
      |        }
      |      }
      |    ]
      |  },
      |  "thirdPartyData": "{}",
      |  "source": "2603a7b5-aaa9-40ad-9598-23f234ec5c32",
      |  "sink": "d7798d5e-b1c6-4027-873e-a6d653957418"
      |}""".stripMargin
}
