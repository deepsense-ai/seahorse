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

package ai.deepsense.workflowmanager.versionconverter

import java.util.UUID

import org.joda.time.DateTime
import spray.json.{JsObject, _}

import ai.deepsense.api.datasourcemanager.model.{AccessLevel, Datasource, DatasourceParams, Visibility}
import ai.deepsense.commons.rest.client.datasources.DatasourceTypes.DatasourceList
import ai.deepsense.commons.utils.Version
import ai.deepsense.deeplang.CatalogRecorder
import ai.deepsense.deeplang.doperations.readwritedatasource.ToDatasourceConverters
import ai.deepsense.deeplang.doperations.{ReadDataFrame, WriteDataFrame}
import ai.deepsense.models.json.graph.GraphJsonProtocol.GraphReader

object VersionConverter extends DefaultJsonProtocol{

  import WorkflowMetadataConverter.Js

   val inputPortChanges13to14: Map[String, Map[Int, Int]] = Map(
    Js.evaluate13id -> Map (0 -> 1, 1 -> 0),
    Js.fit13id -> Map (0 -> 1, 1 -> 0),
    Js.fitPlusTransform13id -> Map (0 -> 1, 1 -> 0),
    Js.gridSearch13id -> Map (0 -> 1, 1 -> 0),
    Js.transform13id -> Map (0 -> 1, 1 -> 0)
  )

  def extractWorkflowVersion(workflow: JsValue): Option[String] = {
    for {
      metadata <- workflow.asJsObject.fields.get(Js.metadata)
      apiVersion <- metadata.asJsObject.fields.get(Js.apiVersion)
    } yield apiVersion.convertTo[String]
  }

  def isCompatibleWith(workflow: JsValue, version: Version): Boolean = {
    extractWorkflowVersion(workflow).exists(Version(_).compatibleWith(version))
  }

  def isVersion13(workflow: JsValue): Boolean = {
    isCompatibleWith(workflow, Version(1, 3, 0))
  }

  def isVersion14(workflow: JsValue): Boolean = {
    isCompatibleWith(workflow, Version(1, 4, 0))
  }

  def convert13to14(workflow: JsValue, ownerId: String, ownerName: String): (JsValue, DatasourceList) = {
    val fields = workflow.asJsObject.fields
    val oldGraph = fields(Js.workflow).asJsObject
    // there is no metadata for inner workflows
    val oldMetadata = fields.getOrElse(Js.metadata, JsObject()).asJsObject
    val (newGraph, newDatasources) = convertGraph13to14(oldGraph, ownerId, ownerName)
    val newMetadata = WorkflowMetadataConverter.convertMetadata(oldMetadata, Version(1, 4, 0).humanReadable)
    (JsObject(fields + (Js.workflow -> newGraph, Js.metadata -> newMetadata)), newDatasources)
  }



  private def convertGraph13to14(graph: JsValue, ownerId: String, ownerName: String): (JsValue, DatasourceList) = {
    val nodes = graph.asJsObject.fields(Js.nodes).asInstanceOf[JsArray]
    val (rdfWdfNodesConverted, datasources) = convertRdfWdfs(nodes, ownerId, ownerName)
    val nodeOperations = createNodesMap(rdfWdfNodesConverted)
    val connections = graph.asJsObject.fields(Js.connections).asInstanceOf[JsArray]
    val newConnections = updateConnections13to14(
      inputPortChanges13to14, nodeOperations, connections
    )
    val (newNodes, innerDatasources) = convertInnerWorkflows13to14(rdfWdfNodesConverted, ownerId, ownerName)
    (JsObject(graph.asJsObject.fields + (Js.nodes -> newNodes, Js.connections -> newConnections)),
      datasources ++ innerDatasources)
  }

  private def extractOperationId(node: JsValue): String =
    node.asJsObject.fields(Js.operation).asJsObject.fields(Js.id).convertTo[String]

  private def convertRdfWdfs(nodesArray: JsArray, ownerId: String, ownerName: String): (JsArray, DatasourceList) = {
    val (rdsInJson, rdss) = convertRdfs(nodesArray, ownerId, ownerName)
    val (wdsInJson, wdss) = convertWdfs(nodesArray, ownerId, ownerName)

    val nodesMap = createNodeByIdMap(nodesArray)
    val rdsMap = createNodeByIdMap(rdsInJson)
    val wdsMap = createNodeByIdMap(wdsInJson)

    (JsArray((nodesMap ++ rdsMap ++ wdsMap).values.toVector), rdss ++ wdss)
  }

  private def createDatasource(dsParams: DatasourceParams, ownerId: String, ownerName: String): Datasource = {
    val newDatasource = new Datasource

    newDatasource.setAccessLevel(AccessLevel.WRITEREAD)
    newDatasource.setCreationDateTime(DateTime.now())
    newDatasource.setParams(dsParams)
    newDatasource.setId(UUID.randomUUID().toString)
    newDatasource.setOwnerId(ownerId)
    newDatasource.setOwnerName(ownerName)
    newDatasource.getParams.setVisibility(Visibility.PUBLICVISIBILITY)

    newDatasource
  }

  private def graphReader = new GraphReader(
    CatalogRecorder.resourcesCatalogRecorder.catalogs.operations)

  private def convertWdfs(nodesArray: JsArray, ownerId: String, ownerName: String): (JsArray, DatasourceList) = {
    val wdfs = nodesArray.elements.filter(extractOperationId(_) == Js.writeDataFrame13Id)

    val (wdss, dss) = wdfs.map { rdf =>
      val paramsJson = rdf.asJsObject.fields(Js.parameters)

      val wdf = new WriteDataFrame
      wdf.setParamsFromJson(paramsJson, graphReader)

      val dsParams = ToDatasourceConverters.wdfToDatasourceParams(wdf)
      val newDatasource = createDatasource(dsParams, ownerId, ownerName)

      (JsObject(
        rdf.asJsObject.fields + (
          Js.operation -> JsObject(
            Js.id -> JsString(Js.writeDatasource14Id),
            Js.name -> JsString(Js.writeDatasourceName)),
          Js.parameters -> JsObject(
            Js.datasourceId -> JsString(newDatasource.getId))
          )
      ), newDatasource)
    }.unzip

    (JsArray(wdss), dss.toList)

  }

  private def convertRdfs(nodesArray: JsArray, ownerId: String, ownerName: String): (JsArray, DatasourceList) = {
    val rdfs = nodesArray.elements.filter(extractOperationId(_) == Js.readDataFrame13Id)

    val (rdss, dss) = rdfs.map { rdf =>
      val paramsJson = rdf.asJsObject.fields(Js.parameters)

      val rdfParams = new ReadDataFrame
      rdfParams.setParamsFromJson(paramsJson, graphReader)

      val dsParams = ToDatasourceConverters.rdfParamsToDatasourceParams(rdfParams)
      val newDatasource = createDatasource(dsParams, ownerId, ownerName)

      (JsObject(
        rdf.asJsObject.fields + (
          Js.operation -> JsObject(
            Js.id -> JsString(Js.readDatasource14Id),
            Js.name -> JsString(Js.readDatasourceName)),
          Js.parameters -> JsObject(
            Js.datasourceId -> JsString(newDatasource.getId))
        )
      ), newDatasource)
    }.unzip

    (JsArray(rdss), dss.toList)
  }

  private def createNodesMap(nodesArray: JsArray): Map[String, String] = {
    createNodeByIdMap(nodesArray).mapValues { e: JsValue =>
        e.asJsObject.fields(Js.operation).asJsObject.fields(Js.id).convertTo[String]
    }
  }

  private def createNodeByIdMap(nodesArray: JsArray): Map[String, JsValue] = {
    nodesArray.elements.map { jsValue =>
      jsValue.asJsObject.fields(Js.id).convertTo[String] ->
      jsValue
    }.toMap
  }

  private def convertInnerWorkflows13to14(
      nodesArray: JsArray,
      ownerId: String,
      ownerName: String):
  (JsArray, DatasourceList) = {
    val toConvert = nodesArray.elements.filter(
      _.asJsObject.fields(Js.operation).asJsObject.fields(Js.id).convertTo[String] == Js.customTransformer13id)

    val (convertedNodes, newDatasources) = toConvert.map { e =>
      val oldParameters = e.asJsObject.fields(Js.parameters).asJsObject
      val oldInnerWorkflow = oldParameters.fields(Js.innerWorkflow).asJsObject
      val (newInnerWorkflow, newDatasources) = convert13to14(oldInnerWorkflow, ownerId, ownerName)
      val newParameters = JsObject(oldParameters.fields.updated(Js.innerWorkflow, newInnerWorkflow))
      (JsObject(e.asJsObject.fields.updated(Js.parameters, newParameters)), newDatasources)
    }.unzip

    (JsArray((createNodeByIdMap(nodesArray) ++ createNodeByIdMap(JsArray(convertedNodes))).values.toVector),
    newDatasources.foldLeft(List[Datasource]())(_ ++ _))

  }

  private def updateConnections13to14(
      inputChanges: Map[String, Map[Int, Int]],
      nodeOperations: Map[String, String],
      connections: JsArray): JsArray = {
    JsArray(connections.elements.map {
      e: JsValue =>
        val oldTo = e.asJsObject.fields(Js.to).asJsObject
        val oldPort = oldTo.fields(Js.portIndex).convertTo[Int]
        val nodeOperation = nodeOperations(oldTo.fields(Js.nodeId).convertTo[String])
        val portChanges = inputChanges.getOrElse(nodeOperation, Map())
        val newPort = portChanges.getOrElse(oldPort, oldPort)
        val newTo = JsObject(oldTo.fields.updated(Js.portIndex, JsNumber(newPort)))
        JsObject(e.asJsObject.fields.updated(Js.to, newTo))
    })
  }
}
