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

package ai.deepsense.deeplang.doperables

import org.apache.spark.SparkContext
import org.apache.spark.sql.types._
import org.mockito.Matchers._
import org.mockito.Mockito.when
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import spray.json.JsObject

import ai.deepsense.deeplang._
import ai.deepsense.deeplang.doperables.InnerWorkflowTestFactory._
import ai.deepsense.deeplang.doperables.dataframe.DataFrame
import ai.deepsense.deeplang.doperations.ConvertType
import ai.deepsense.deeplang.inference.InferContext
import ai.deepsense.deeplang.params.Param
import ai.deepsense.deeplang.params.custom.{InnerWorkflow, PublicParam}
import ai.deepsense.models.json.graph.GraphJsonProtocol.GraphReader
import ai.deepsense.sparkutils.SparkSQLSession

class CustomTransformerSpec extends UnitSpec {
  val graphReader = mock[GraphReader]
  "CustomTransfromer" should {

    "execute inner workflow" in {
      val workflow = InnerWorkflow(simpleGraph(graphReader), JsObject())
      val outputDataFrame = mock[DataFrame]

      val innerWorkflowExecutor = mock[InnerWorkflowExecutor]
      when(innerWorkflowExecutor.execute(any(), same(workflow), any())).thenReturn(outputDataFrame)

      val context = mockExecutionContext(innerWorkflowExecutor)

      val transformer = new CustomTransformer(workflow, Seq.empty)

      transformer._transform(context, mock[DataFrame]) shouldBe outputDataFrame
    }

    "infer knowledge" in {
      val inferContext = MockedInferContext()

      val transformer = new CustomTransformer(InnerWorkflow(simpleGraph(graphReader), JsObject()), Seq.empty)

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

    "replicate" in {
      val publicParam = TypeConverter().targetType.replicate("public name")
      val publicParamsWithValues = Seq(ParamWithValues(publicParam))
      val params: Array[Param[_]] = publicParamsWithValues.map(_.param).toArray

      val workflow = InnerWorkflow(simpleGraph(graphReader), JsObject(),
        List(PublicParam(innerNodeId, "target type", "public name")))
      val transformer = new CustomTransformer(workflow, publicParamsWithValues)

      val replicated = transformer.replicate()
      replicated.innerWorkflow shouldBe workflow
      replicated.params shouldBe params
    }

    "replicate with set values" in {
      val publicParam = TypeConverter().targetType.replicate("public name")
      val defaultValue = TargetTypeChoices.IntegerTargetTypeChoice()
      val setValue = TargetTypeChoices.StringTargetTypeChoice()
      val publicParamsWithValues =
        Seq(ParamWithValues(publicParam, Some(defaultValue), Some(setValue)))
      val params: Array[Param[_]] = publicParamsWithValues.map(_.param).toArray

      val workflow = InnerWorkflow(simpleGraph(graphReader), JsObject(),
        List(PublicParam(innerNodeId, "target type", "public name")))
      val transformer = new CustomTransformer(workflow, publicParamsWithValues)

      val replicated = transformer.replicate()
      replicated.innerWorkflow shouldBe workflow
      replicated.params shouldBe params
      replicated.getDefault(publicParam) shouldBe Some(defaultValue)
      replicated.get(publicParam) shouldBe Some(setValue)
    }

    "set public params" when {
      "executing inner workflow" in {
        val innerParam = TypeConverter().targetType
        val publicParam = TypeConverter().targetType.replicate("public name")
        val publicParamsWithValues = Seq(ParamWithValues(publicParam))

        val customTargetType = TargetTypeChoices.LongTargetTypeChoice()

        val workflow = InnerWorkflow(simpleGraph(graphReader), JsObject(),
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

        val transformer = new CustomTransformer(workflow, publicParamsWithValues)
        transformer.set(publicParam -> customTargetType)

        transformer._transform(context, mock[DataFrame]) shouldBe outputDataFrame
      }

      "inferring schema" in {
        val inferContext = MockedInferContext()

        val publicParam = TypeConverter().targetType.replicate("public name")
        val publicParamsWithValues = Seq(ParamWithValues(publicParam))

        val transformer = new CustomTransformer(InnerWorkflow(simpleGraph(graphReader), JsObject(),
          List(PublicParam(innerNodeId, "target type", "public name"))), publicParamsWithValues)

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
      mock[SparkSQLSession],
      mock[InferContext],
      ExecutionMode.Batch,
      mock[FileSystemClient],
      "/tmp",
      "/tmp/library",
      innerWorkflowExecutor,
      mock[ContextualDataFrameStorage],
      None,
      None,
      mock[ContextualCustomCodeExecutor]
    )
}
