/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.graphexecutor

import io.deepsense.deeplang.doperations.{DataFrameSplitter, ReadDataFrame, WriteDataFrame}
import io.deepsense.graph.Edge

class GraphWithSplitterIntegSuite extends GraphExecutionIntegSuite {

  def experimentName = "(ReadDF, Split, 2xWriteDF)"

  import io.deepsense.deeplang.doperations.ReadDataFrame._
  val readOp = new ReadDataFrame
  readOp.parameters.getStringParameter(idParam).value =
    Some(SimpleGraphExecutionIntegSuiteEntities.entityUuid)

  val splitOp = new DataFrameSplitter
  splitOp.parameters.getNumericParameter(splitOp.splitRatioParam).value = Some(0.2)
  splitOp.parameters.getNumericParameter(splitOp.seedParam).value = Some(1)

  import io.deepsense.deeplang.doperations.WriteDataFrame._
  val writeOpLeft = new WriteDataFrame
  writeOpLeft.parameters.getStringParameter(nameParam).value = Some("left name")
  writeOpLeft.parameters.getStringParameter(descriptionParam).value = Some("left description")

  val writeOpRight = new WriteDataFrame
  writeOpRight.parameters.getStringParameter(nameParam).value = Some("right name")
  writeOpRight.parameters.getStringParameter(descriptionParam).value = Some("right description")

  val nodes = Seq(node(readOp), node(splitOp), node(writeOpLeft), node(writeOpRight))

  val edges = Seq(
    Edge(nodes(0), 0, nodes(1), 0),
    Edge(nodes(1), 0, nodes(2), 0),
    Edge(nodes(1), 1, nodes(3), 0)
  )
}
