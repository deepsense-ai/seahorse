/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.workflowmanager.versionconverter

import java.util.UUID

import org.joda.time.DateTime
import spray.json._

import io.deepsense.api.datasourcemanager.model.{AccessLevel, Datasource, DatasourceParams, Visibility}
import io.deepsense.commons.rest.client.datasources.DatasourceTypes.DatasourceList
import io.deepsense.commons.utils.Version
import io.deepsense.deeplang.doperations.readwritedatasource.ToDatasourceConverters
import io.deepsense.deeplang.doperations.{ReadDataFrame, WriteDataFrame}

object VersionConverter extends DefaultJsonProtocol{

  private object Js {
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
    val newMetadata = convertMetadata13to14(oldMetadata)
    (JsObject(fields + (Js.workflow -> newGraph, Js.metadata -> newMetadata)), newDatasources)
  }

   def convertMetadata13to14(metadata: JsValue): JsValue =
    JsObject(metadata.asJsObject.fields.updated(Js.apiVersion, JsString(Js.apiVersion14)))

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

  private def convertWdfs(nodesArray: JsArray, ownerId: String, ownerName: String): (JsArray, DatasourceList) = {
    val wdfs = nodesArray.elements.filter(extractOperationId(_) == Js.writeDataFrame13Id)

    val (wdss, dss) = wdfs.map { rdf =>
      val paramsJson = rdf.asJsObject.fields(Js.parameters)

      val wdf = new WriteDataFrame

      wdf.setParamsFromJson(paramsJson)

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

      rdfParams.setParamsFromJson(paramsJson)

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
