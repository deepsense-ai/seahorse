/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.models.json.workflow

import scala.reflect.runtime.{universe => ru}

import org.mockito.Matchers._
import org.mockito.Mockito._

import io.deepsense.deeplang.catalogs.doperable.DOperableCatalog
import io.deepsense.deeplang.inference.{InferContext, InferenceWarnings}
import io.deepsense.deeplang.parameters.ParametersSchema
import io.deepsense.deeplang.{DKnowledge, DOperable, DOperation}
import io.deepsense.graph.{Edge, Endpoint, Graph, Node}
import io.deepsense.models.json.graph.GraphJsonProtocol.GraphReader
import io.deepsense.models.json.{StandardSpec, UnitTestSupport}

class WorkflowJsonProtocolSpec
  extends StandardSpec
  with UnitTestSupport
  with WorkflowJsonProtocol
  with HierarchyDescriptorJsonProtocol {

  val operable = mockOperable()

  val dOperableCatalog = mock[DOperableCatalog]
  when(dOperableCatalog.concreteSubclassesInstances(
    any(classOf[ru.TypeTag[DOperable]]))).thenReturn(Set(operable))

  override val inferContext: InferContext = mock[InferContext]
  when(inferContext.dOperableCatalog).thenReturn(dOperableCatalog)

  override val graphReader: GraphReader = mock[GraphReader]

  val operation1 = mockOperation(0, 1, DOperation.Id.randomId, "name1", "version1")
  val operation2 = mockOperation(1, 1, DOperation.Id.randomId, "name2", "version2")
  val operation3 = mockOperation(1, 1, DOperation.Id.randomId, "name3", "version3")
  val operation4 = mockOperation(2, 1, DOperation.Id.randomId, "name4", "version4")

  val node1 = Node(Node.Id.randomId, operation1)
  val node2 = Node(Node.Id.randomId, operation2)
  val node3 = Node(Node.Id.randomId, operation3)
  val node4 = Node(Node.Id.randomId, operation4)
  val nodes = Set(node1, node2, node3, node4)
  val preEdges = Set(
    (node1, node2, 0, 0),
    (node1, node3, 0, 0),
    (node2, node4, 0, 0),
    (node3, node4, 0, 1))
  val edges = preEdges.map(n => Edge(Endpoint(n._1.id, n._3), Endpoint(n._2.id, n._4)))
  val graph = Graph(nodes, edges)

  // TODO add tests

  def mockOperation(
    inArity: Int,
    outArity: Int,
    id: DOperation.Id,
    name: String,
    version: String): DOperation = {
    val dOperation = mock[DOperation]
    when(dOperation.id).thenReturn(id)
    when(dOperation.name).thenReturn(name)
    when(dOperation.version).thenReturn(version)
    when(dOperation.inArity).thenReturn(inArity)
    when(dOperation.outArity).thenReturn(outArity)
    when(dOperation.inPortTypes).thenReturn(
      Vector.fill(inArity)(implicitly[ru.TypeTag[DOperable]]))
    when(dOperation.outPortTypes).thenReturn(
      Vector.fill(outArity)(implicitly[ru.TypeTag[DOperable]]))
    val operableMock = mockOperable()
    val knowledge = mock[DKnowledge[DOperable]]
    when(knowledge.types).thenReturn(Set[DOperable](operableMock))
    when(knowledge.filterTypes(any())).thenReturn(knowledge)
    when(dOperation.inferKnowledge(anyObject())(anyObject())).thenReturn(
      (Vector.fill(outArity)(knowledge), InferenceWarnings.empty))
    val parametersSchema = mock[ParametersSchema]
    when(dOperation.parameters).thenReturn(parametersSchema)
    dOperation
  }

  def mockOperable(): DOperable = {
    val dOperable = mock[DOperable]
    when(dOperable.inferredMetadata).thenReturn(None)
    dOperable
  }
}
