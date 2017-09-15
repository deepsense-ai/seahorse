/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.workflowmanager.versionconverter

import spray.json._

object VersionConverter extends DefaultJsonProtocol{
  private val apiVersion14 = "1.4.0"
  private val apiVersion13 = "1.3.0"
  private val apiVersion12 = "1.2.0"

  private val evaluate13id = "a88eaf35-9061-4714-b042-ddd2049ce917"
  private val fit13id = "0c2ff818-977b-11e5-8994-feff819cdc9f"
  private val fitPlusTransform13id = "1cb153f1-3731-4046-a29b-5ad64fde093f"
  private val gridSearch13id = "9163f706-eaaf-46f6-a5b0-4114d92032b7"
  private val transform13id = "643d8706-24db-4674-b5b4-10b5129251fc"
  private val customTransformer13id = "65240399-2987-41bd-ba7e-2944d60a3404"

  private val jsApiVersion = "apiVersion"
  private val jsConnections = "connections"
  private val jsId = "id"
  private val jsInnerWorkflow = "inner workflow"
  private val jsMetadata = "metadata"
  private val jsNodeId = "nodeId"
  private val jsNodes = "nodes"
  private val jsOperation = "operation"
  private val jsParameters = "parameters"
  private val jsPortIndex = "portIndex"
  private val jsTo = "to"
  private val jsWorkflow = "workflow"

  private val inputPortChanges13to14: Map[String, Map[Int, Int]] = Map(
    evaluate13id -> Map (0 -> 1, 1 -> 0),
    fit13id -> Map (0 -> 1, 1 -> 0),
    fitPlusTransform13id -> Map (0 -> 1, 1 -> 0),
    gridSearch13id -> Map (0 -> 1, 1 -> 0),
    transform13id -> Map (0 -> 1, 1 -> 0)
  )


  def convert13to14(workflow: JsValue): JsValue = {
    val fields = workflow.asJsObject.fields
    val oldGraph = fields(jsWorkflow).asJsObject
    // there is no metadata for inner workflows
    val oldMetadata = fields.getOrElse(jsMetadata, JsObject()).asJsObject
    val newGraph = convertGraph13to14(oldGraph)
    val newMetadata = convertMetadata13to14(oldMetadata)
    JsObject(fields + (jsWorkflow -> newGraph, jsMetadata -> newMetadata))
  }

  private def convertMetadata13to14(metadata: JsValue): JsValue =
    JsObject(metadata.asJsObject.fields.updated(jsApiVersion, JsString(apiVersion14)))

  private def convertGraph13to14(graph: JsValue): JsValue = {
    val nodes = graph.asJsObject.fields(jsNodes).asInstanceOf[JsArray]
    val nodeOperations = createNodesMap(nodes)
    val connections = graph.asJsObject.fields(jsConnections).asInstanceOf[JsArray]
    val newConnections = updateConnections13to14(
      inputPortChanges13to14, nodeOperations, connections
    )
    val newNodes = convertInnerWorkflows13to14(nodes)
    JsObject(graph.asJsObject.fields + (jsNodes -> newNodes, jsConnections -> newConnections))
  }

  private def createNodesMap(nodesArray: JsArray): Map[String, String] = {
    nodesArray.elements.map{
      e: JsValue =>
        e.asJsObject.fields(jsId).convertTo[String] ->
        e.asJsObject.fields(jsOperation).asJsObject.fields(jsId).convertTo[String]
    }.toMap
  }

  private def convertInnerWorkflows13to14(nodesArray: JsArray): JsArray = {
    JsArray(nodesArray.elements.map {
      e: JsValue =>
        if (e.asJsObject.fields(jsOperation).asJsObject.fields(jsId).convertTo[String] == customTransformer13id) {
          val oldParameters = e.asJsObject.fields(jsParameters).asJsObject
          val oldInnerWorkflow = oldParameters.fields(jsInnerWorkflow).asJsObject
          val newInnerWorkflow = convert13to14(oldInnerWorkflow)
          val newParameters = JsObject(oldParameters.fields.updated(jsInnerWorkflow, newInnerWorkflow))
          JsObject(e.asJsObject.fields.updated(jsParameters, newParameters))
      } else {
        e
      }
    })
  }

  private def updateConnections13to14(
      inputChanges: Map[String, Map[Int, Int]],
      nodeOperations: Map[String, String],
      connections: JsArray): JsArray = {
    JsArray(connections.elements.map{
      e: JsValue =>
        val oldTo = e.asJsObject.fields(jsTo).asJsObject
        val oldPort = oldTo.fields(jsPortIndex).convertTo[Int]
        val nodeOperation = nodeOperations(oldTo.fields(jsNodeId).convertTo[String])
        val portChanges = inputChanges.getOrElse(nodeOperation, Map())
        val newPort = portChanges.getOrElse(oldPort, oldPort)
        val newTo = JsObject(oldTo.fields.updated(jsPortIndex, JsNumber(newPort)))
        JsObject(e.asJsObject.fields.updated(jsTo, newTo))
    })
  }
}
