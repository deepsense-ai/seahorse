/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.experimentmanager.execution

import io.deepsense.deeplang.doperations.{DecomposeDatetime, LoadDataFrame, SaveDataFrame}
import io.deepsense.deeplang.parameters.NameSingleColumnSelection
import io.deepsense.graph._
import io.deepsense.graphexecutor.SimpleGraphExecutionIntegSuiteEntities

class GraphWithTimestampDecomposeIntegSuite extends ExperimentExecutionSpec {

  override def executionTimeLimitSeconds = 120L

  override def experimentName = "(LoadDF, DecomposeTimestamp, SaveDF)"

  override def esFactoryName = SimpleGraphExecutionIntegSuiteEntities.Name

  override def tenantId = SimpleGraphExecutionIntegSuiteEntities.entityTenantId

  override def requiredFiles: Map[String, String] =
    Map("/SimpleDataFrame" -> SimpleGraphExecutionIntegSuiteEntities.dataFrameLocation)

  import io.deepsense.deeplang.doperations.LoadDataFrame._
  val loadOp = new LoadDataFrame
  loadOp.parameters.getStringParameter(idParam).value =
    Some(SimpleGraphExecutionIntegSuiteEntities.entityId.toString)

  val timestampDecomposerOp = DecomposeDatetime()
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
