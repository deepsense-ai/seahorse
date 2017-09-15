/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.graphexecutor

import io.deepsense.deeplang.doperations.{ReadDataFrame, TimestampDecomposer, WriteDataFrame}
import io.deepsense.deeplang.parameters.NameSingleColumnSelection
import io.deepsense.graph._

class GraphWithTimestampDecomposeIntegSuite extends GraphExecutionIntegSuite {

  def experimentName = "(ReadDF, DecomposeTimestamp, WriteDF)"

  import io.deepsense.deeplang.doperations.ReadDataFrame._
  val readOp = new ReadDataFrame
  readOp.parameters.getStringParameter(idParam).value =
    Some(SimpleGraphExecutionIntegSuiteEntities.entityUuid)

  val timestampDecomposerOp = new TimestampDecomposer
  timestampDecomposerOp.parameters.getSingleColumnSelectorParameter("timestampColumn").value =
    Some(NameSingleColumnSelection("column4"))
  timestampDecomposerOp.parameters.getMultipleChoiceParameter("parts").value =
    Some(Seq("year", "month", "day", "hour", "minutes", "seconds"))

  import io.deepsense.deeplang.doperations.WriteDataFrame._
  val writeOp = new WriteDataFrame
  writeOp.parameters.getStringParameter(nameParam).value = Some("left name")
  writeOp.parameters.getStringParameter(descriptionParam).value = Some("left description")

  val nodes = Seq(node(readOp), node(timestampDecomposerOp), node(writeOp))

  val edges = Seq(
    Edge(nodes(0), 0, nodes(1), 0),
    Edge(nodes(1), 0, nodes(2), 0)
  )
}
