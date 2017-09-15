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

package ai.deepsense.workflowmanager.storage

import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{Matchers, WordSpec}
import spray.json.{DefaultJsonProtocol, JsObject}

import ai.deepsense.deeplang.DOperation
import ai.deepsense.graph.Endpoint

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
      name: String): DOperation = {
    val dOperation = mock[DOperation]
    when(dOperation.inArity).thenReturn(inArity)
    when(dOperation.outArity).thenReturn(outArity)
    when(dOperation.id).thenReturn(id)
    when(dOperation.name).thenReturn(name)
    when(dOperation.paramValuesToJson).thenReturn(JsObject())
    dOperation
  }
}
