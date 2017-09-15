/**
 * Copyright 2017 deepsense.ai (CodiLime, Inc)
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
package ai.deepsense.workflowmanager.versionconverter

import spray.json.{JsObject, JsString, JsValue}

import ai.deepsense.commons.utils.Version


object WorkflowMetadataConverter {

  object Js {
    val apiVersion14 = "1.4.0"
    val apiVersion13 = "1.3.0"
    val apiVersion12 = "1.2.0"

    val evaluate13id = "a88eaf35-9061-4714-b042-ddd2049ce917"
    val fit13id = "0c2ff818-977b-11e5-8994-feff819cdc9f"
    val fitPlusTransform13id = "1cb153f1-3731-4046-a29b-5ad64fde093f"
    val gridSearch13id = "9163f706-eaaf-46f6-a5b0-4114d92032b7"
    val transform13id = "643d8706-24db-4674-b5b4-10b5129251fc"
    val customTransformer13id = "65240399-2987-41bd-ba7e-2944d60a3404"
    val readDataFrame13Id = "c48dd54c-6aef-42df-ad7a-42fc59a09f0e"
    val writeDataFrame13Id = "9e460036-95cc-42c5-ba64-5bc767a40e4e"
    val readDatasource14Id = "1a3b32f0-f56d-4c44-a396-29d2dfd43423"
    val writeDatasource14Id = "bf082da2-a0d9-4335-a62f-9804217a1436"
    val readDatasourceName = "Read Datasource"
    val writeDatasourceName = "Write Datasource"

    val apiVersion = "apiVersion"
    val connections = "connections"
    val id = "id"
    val name = "name"
    val innerWorkflow = "inner workflow"
    val metadata = "metadata"
    val nodeId = "nodeId"
    val nodes = "nodes"
    val operation = "operation"
    val parameters = "parameters"
    val portIndex = "portIndex"
    val to = "to"
    val workflow = "workflow"
    val datasourceId = "data source"
  }

  def setWorkflowVersion(workflow: JsValue, targetVersion: Version): JsValue = {
    val fields = workflow.asJsObject.fields
    val oldMetadata = fields.getOrElse(Js.metadata, JsObject()).asJsObject
    val newMetadata = convertMetadata(oldMetadata, targetVersion.humanReadable)
    JsObject(fields + (Js.metadata -> newMetadata))
  }

  def convertMetadata(metadata: JsValue, targetVersion: String): JsValue =
    JsObject(metadata.asJsObject.fields.updated(Js.apiVersion, new JsString(targetVersion)))
}
