/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.graphexecutor

import io.deepsense.deeplang.doperations.{LoadDataFrame, TimestampDecomposer, SaveDataFrame}
import io.deepsense.deeplang.parameters.NameSingleColumnSelection
import io.deepsense.graph._

class GraphWithTimestampDecomposeIntegSuite extends GraphExecutionIntegSuite {

  override def experimentName = "(LoadDF, DecomposeTimestamp, SaveDF)"

  override def esFactoryName = SimpleGraphExecutionIntegSuiteEntities.Name

  override def tenantId = SimpleGraphExecutionIntegSuiteEntities.entityTenantId

  override def requiredFiles: Map[String, String] =
    Map("/SimpleDataFrame" -> SimpleGraphExecutionIntegSuiteEntities.dataFrameLocation)

  import io.deepsense.deeplang.doperations.LoadDataFrame._
  val loadOp = new LoadDataFrame
  loadOp.parameters.getStringParameter(idParam).value =
    Some(SimpleGraphExecutionIntegSuiteEntities.entityUuid)

  val timestampDecomposerOp = new TimestampDecomposer
  timestampDecomposerOp.parameters.getSingleColumnSelectorParameter("timestampColumn").value =
    Some(NameSingleColumnSelection("column4"))
  timestampDecomposerOp.parameters.getMultipleChoiceParameter("parts").value =
    Some(Seq("year", "month", "day", "hour", "minutes", "seconds"))

  import io.deepsense.deeplang.doperations.SaveDataFrame._
  val saveOp = new SaveDataFrame
  saveOp.parameters.getStringParameter(nameParam).value = Some("left name")
  saveOp.parameters.getStringParameter(descriptionParam).value = Some("left description")

  val nodes = Seq(node(loadOp), node(timestampDecomposerOp), node(saveOp))

  val edges = Seq(
    Edge(nodes(0), 0, nodes(1), 0),
    Edge(nodes(1), 0, nodes(2), 0)
  )
}
