/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.experimentmanager.storage.cassandra

import com.datastax.driver.core.Row
import com.google.inject.Inject

import io.deepsense.commons.datetime.DateTimeConverter
import io.deepsense.commons.exception.FailureDescription
import io.deepsense.graph.Graph
import io.deepsense.graphjson.GraphJsonProtocol.GraphReader
import io.deepsense.models.experiments.Experiment
import io.deepsense.commons.exception.json.FailureDescriptionJsonProtocol

import spray.json._


class ExperimentRowMapper @Inject() (graphReader: GraphReader)
    extends FailureDescriptionJsonProtocol {

  def fromRow(row: Row): Experiment = {
    Experiment(
      id = Experiment.Id(row.getUUID(ExperimentRowMapper.Id)),
      tenantId = row.getString(ExperimentRowMapper.TenantId),
      name = row.getString(ExperimentRowMapper.Name),
      graph = row.getString(ExperimentRowMapper.Graph).parseJson.convertTo[Graph](graphReader),
      created = DateTimeConverter.fromMillis(row.getDate(ExperimentRowMapper.Created).getTime),
      updated = DateTimeConverter.fromMillis(row.getDate(ExperimentRowMapper.Updated).getTime),
      description = row.getString(ExperimentRowMapper.Description),
      state = getState(
        row.getString(ExperimentRowMapper.StateStatus),
        row.getString(ExperimentRowMapper.StateDescription))
    )
  }

  def getState(status: String, description: String): Experiment.State = {
    val failureDescription = Option(description) match {
      case Some(s) => Some(s.parseJson.convertTo[FailureDescription])
      case _ => None
    }
    val statusEnum = Experiment.Status.withName(status)
    Experiment.State(statusEnum, failureDescription)
  }
}

object ExperimentRowMapper {
  val Id = "id"
  val TenantId = "tenantid"
  val Name = "name"
  val Description = "description"
  val Graph = "graph"
  val Created = "created"
  val Updated = "updated"
  val StateStatus = "state_status"
  val StateDescription = "state_description"
  val Deleted = "deleted"
}
