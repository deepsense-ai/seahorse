package io.deepsense.workflowmanager.storage.cassandra

import scala.collection.JavaConversions._

import com.datastax.driver.core.Row
import com.google.inject.Inject
import spray.json._

import io.deepsense.deeplang.inference.InferContext
import io.deepsense.models.json.graph.GraphJsonProtocol.GraphReader
import io.deepsense.models.json.workflow.WorkflowWithResultsJsonProtocol
import io.deepsense.models.workflows.WorkflowWithResults

class WorkflowResultsRowMapper @Inject() (
  override val graphReader: GraphReader)
  extends WorkflowWithResultsJsonProtocol {

  def fromRow(row: Row): List[WorkflowWithResults] = {
    row.getList[String](WorkflowResultsRowMapper.Results, classOf[String])
      .toList.map(_.parseJson.convertTo[WorkflowWithResults])
  }

  def resultToCell(result: WorkflowWithResults): String = result.toJson.compactPrint
}

object WorkflowResultsRowMapper {
  val Id = "id"
  val Results = "results"
}
