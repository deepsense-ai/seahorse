/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Wojciech Jurczyk
 */

package io.deepsense.graphjson

import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import org.scalatest.{Matchers, WordSpec}
import spray.json.{DefaultJsonProtocol, JsObject}

import io.deepsense.deeplang.DOperation
import io.deepsense.deeplang.parameters.ParametersSchema
import io.deepsense.graph.Endpoint

trait GraphJsonTestSupport
  extends WordSpec
  with MockitoSugar
  with DefaultJsonProtocol
  with Matchers {

  def assertEndpointMatchesJsObject(edgeEnd: Endpoint, edgeEndJs: JsObject): Unit = {
    assert(edgeEndJs.fields("nodeId").convertTo[String] == edgeEnd.nodeId.value.toString)
    assert(edgeEndJs.fields("portIndex").convertTo[Int] == edgeEnd.portIndex)
  }

  def endpointMatchesJsObject(edgeEnd: Endpoint, edgeEndJs: JsObject): Boolean = {
    edgeEndJs.fields("nodeId").convertTo[String] == edgeEnd.nodeId.value.toString &&
    edgeEndJs.fields("portIndex").convertTo[Int] == edgeEnd.portIndex
  }

  def mockOperation(
      inArity: Int,
      outArity: Int,
      id: DOperation.Id,
      name: String,
      version: String): DOperation = {
    val dOperation = mock[DOperation]
    when(dOperation.inArity).thenReturn(inArity)
    when(dOperation.outArity).thenReturn(outArity)
    when(dOperation.id).thenReturn(id)
    when(dOperation.name).thenReturn(name)
    when(dOperation.version).thenReturn(version)
    val mockParameters = mock[ParametersSchema]
    when(dOperation.parameters).thenReturn(mockParameters)
    dOperation
  }
}
