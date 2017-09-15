/**
 * Copyright 2017, deepsense.ai
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

package io.deepsense.workflowmanager.versionconverter

import java.util.UUID

import org.apache.commons.io.IOUtils
import org.scalactic.Equality
import org.scalatest.{Matchers, WordSpec}
import spray.json._

import io.deepsense.api.datasourcemanager.model.{Datasource, DatasourceParams}
import io.deepsense.commons.utils.Version

class VersionConverterSpec extends WordSpec with Matchers {

  val uutName = VersionConverter.getClass.getSimpleName.filterNot(_ == '$')

  trait Setup extends DefaultJsonProtocol {

    val uut = VersionConverter

    val readDatasourceId = "1a3b32f0-f56d-4c44-a396-29d2dfd43423"
    val writeDatasourceId = "bf082da2-a0d9-4335-a62f-9804217a1436"

    val readDataFrameId = "c48dd54c-6aef-42df-ad7a-42fc59a09f0e"
    val writeDataFrameId = "9e460036-95cc-42c5-ba64-5bc767a40e4e"

    val customTransformerId = "65240399-2987-41bd-ba7e-2944d60a3404"

    val workflowResourceName = "versionconverter/workflow_1_3_2.json.ignored"

    val ownerId = UUID.fromString("0-0-0-0-0")
    val ownerName = "seahorse test"

    lazy val workflowJson: JsValue =
      IOUtils.toString(getClass.getClassLoader.getResourceAsStream(workflowResourceName)).parseJson

    def extractOperationId(node: JsValue): String =
      node.asJsObject.fields("operation").asJsObject.fields("id").convertTo[String]

    def testNotNulls[A, B](a: A, b: B, p: A => B => Boolean): Boolean = {
      (Option(a), Option(b)) match {
        case (None, None) => true
        case (Some(_), None) => false
        case (None, Some(_)) => false
        case (Some(aa), Some(bb)) => p(aa)(bb)
      }
    }

    implicit object datasourceParamsEquality extends Equality[DatasourceParams] {
      override def areEqual(a: DatasourceParams, b: Any): Boolean =
        testNotNulls[DatasourceParams, Any](a, b, aa => bb =>
          implicitly[Equality[String]].areEqual(aa.getName,
            b.asInstanceOf[DatasourceParams].getName)
        )
    }

    implicit object datasourceEquality extends Equality[Datasource] {
      override def areEqual(a: Datasource, b: Any): Boolean =
        testNotNulls[Datasource, Any](a, b, aa => bb =>
          implicitly[Equality[DatasourceParams]].areEqual(aa.getParams,
            bb.asInstanceOf[Datasource].getParams))
    }

    def flattenWorkflows(nodes: Seq[JsValue]): Seq[JsValue] = {
      nodes.flatMap { n =>
        if (extractOperationId(n) == customTransformerId) {
          n.asJsObject.fields("parameters")
            .asJsObject.fields("inner workflow")
            .asJsObject.fields("workflow")
            .asJsObject.fields("nodes").asInstanceOf[JsArray].elements
        } else {
          Seq(n)
        }
      }
    }
  }

  s"A $uutName" should {

    "convert 1.3 workflow to 1.4" in {

      new Setup {

        val (convertedWorkflow, newDatasources) = uut.convert13to14(workflowJson, ownerId.toString, ownerName)

        newDatasources should have length 3

        val expectedDs1 = new Datasource
        expectedDs1.setParams(new DatasourceParams)
        expectedDs1.getParams.setName("adult.data")
        val expectedDs2 = new Datasource
        expectedDs2.setParams(new DatasourceParams)
        expectedDs2.getParams.setName("transactions.csv")
        val expectedDs3 = new Datasource
        expectedDs3.setParams(new DatasourceParams)
        expectedDs3.getParams.setName("evaluate.csv")

        newDatasources should contain theSameElementsAs Set(expectedDs1, expectedDs2, expectedDs3)

        val nodes =
          convertedWorkflow.asJsObject.fields("workflow").asJsObject.fields("nodes").asInstanceOf[JsArray].elements

        // or you could write another Equality instance
        assert(flattenWorkflows(nodes).forall { n =>
          val operationId = extractOperationId(n)

          operationId != readDataFrameId && operationId != writeDataFrameId
        })

        val workflowDatasourceIds = for {
          n <- flattenWorkflows(nodes)
          operationId = extractOperationId(n)
          if operationId == readDatasourceId || operationId == writeDatasourceId
        } yield {
          n.asJsObject.fields("parameters").asJsObject.fields("data source").convertTo[String]
        }

        val datasourceIds = newDatasources.map(_.getId)

        workflowDatasourceIds should contain theSameElementsAs datasourceIds

      }
    }

  }


  "WorkflowMetadataConverter" should {
    "convert 1.4 workflow to 1.5" in {
      new Setup {
        override val workflowResourceName = "versionconverter/empty_workflow_1.4.1.json"
        val convertedWorkflow = WorkflowMetadataConverter.setWorkflowVersion(workflowJson, Version(1, 5, 0))
        val metadata = convertedWorkflow.asJsObject.fields("metadata").asJsObject
        metadata.fields("apiVersion").asInstanceOf[JsString].value shouldBe "1.5.0"
      }
    }
  }
}
